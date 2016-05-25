/**
 * 
 */
package shape;

import static shape.ShapeConventions.DimName.R;
import static shape.Shape.WhereAmI.DEFINED;
import static shape.Shape.WhereAmI.INSIDE;
import static shape.Shape.WhereAmI.UNDEFINED;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.SpatialGrid;
import linearAlgebra.Vector;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelMaker.Requirement;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import shape.Dimension.DimensionMaker;
import shape.ShapeConventions.DimName;
import shape.resolution.ResolutionCalculator;
import shape.resolution.ResolutionCalculator.ResCalc;
import shape.subvoxel.SubvoxelPoint;
import surface.Plane;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;
/**
 * \brief Abstract class for all shape objects.
 * 
 * <p>These are typically used by {@code Compartment}s; cell shapes are
 * currently described using {@code Body} and {@code Surface} objects.</p>
 * 
 * <p>This file is structured like so:<ul>
 * <li>Construction</li>
 * <li>Basic setters & getters</li>
 * <li>Dimensions</li>
 * <li>Surfaces</li>
 * <li>Boundaries</li>
 * <li>Voxels</li>
 * <li>Sub-voxel points</li>
 * <li>Coordinate iterator</li>
 * <li>Neighbor iterator</li>
 * </ul></p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 * 								Friedrich-Schiller University Jena, Germany 
 */
// TODO remove the last three sections by incorporation into Node construction.
public abstract class Shape implements
					CanPrelaunchCheck, IsSubmodel, XMLable, NodeConstructor
{
	protected enum WhereAmI
	{
		/**
		 * Inside the array.
		 */
		INSIDE,
		/**
		 * On a defined boundary.
		 */
		DEFINED,
		/**
		 * On an undefined boundary.
		 */
		UNDEFINED;
	}
	
	/**
	 * TODO
	 */
	protected ModelNode _modelNode;
	/**
	 * Ordered dictionary of dimensions for this shape.
	 */
	protected LinkedHashMap<DimName, Dimension> _dimensions = 
									new LinkedHashMap<DimName, Dimension>();
	/**
	 * Storage container for dimensions that this {@code Shape} is not yet
	 * ready to initialise.
	 */
	protected HashMap<DimName,ResCalc> _rcStorage =
												new HashMap<DimName,ResCalc>();
	/**
	 * The greatest potential flux between neighboring voxels. Multiply by 
	 * diffusivity to become actual flux.
	 */
	protected Double _maxFluxPotentl = null;
	/**
	 * Surfaces for collision detection methods.
	 */
	protected Map<Surface,Boundary> _surfaces = 
			new HashMap<Surface,Boundary>();
	
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected Collection<Boundary> _otherBoundaries = 
													new LinkedList<Boundary>();
	/**
	 * Current coordinate considered by the internal iterator.
	 */
	protected int[] _currentCoord;
	/**
	 * The number of voxels, in each dimension, for the current coordinate of
	 * the internal iterator.
	 */
	protected int[] _currentNVoxel;
	/**
	 * Current neighbour coordinate considered by the neighbor iterator.
	 */
	protected int[] _currentNeighbor;
	/**
	 * the dimension name the current neighbor is moving in
	 */
	protected DimName _nbhDimName;
	/**
	 * Integer indicating positive (1) or negative (0) relative position
	 * to the current coordinate.
	 */
	protected int _nbhDirection;
	/**
	 * TODO
	 */
	protected WhereAmI _whereIsNbh;
	
	/**
	 * A helper vector for finding the location of the origin of a voxel.
	 */
	protected final static double[] VOXEL_ORIGIN_HELPER = Vector.vector(3,0.0);
	/**
	 * A helper vector for finding the location of the centre of a voxel.
	 */
	protected final static double[] VOXEL_CENTRE_HELPER = Vector.vector(3,0.5);
	/**
	 * A helper vector for finding the 'upper most' location of a voxel.
	 */
	protected final static double[] VOXEL_All_ONE_HELPER = Vector.vector(3,1.0);
	
	
	/*************************************************************************
	 * CONSTRUCTION
	 ************************************************************************/
	
	@Override
	public ModelNode getNode()
	{
		if ( this._modelNode == null )
		{
			ModelNode myNode = new ModelNode(XmlLabel.compartmentShape, this);
			myNode.requirement = Requirements.EXACTLY_ONE;
			myNode.add(new ModelAttribute(XmlLabel.classAttribute, 
											this.getName(), null, false ));
			this._modelNode = myNode;
		}
		return this._modelNode;
	}

	@Override
	public void setNode(ModelNode node)
	{
		// TODO check if a node is being overwritten?
		this._modelNode = node;
	}

	@Override
	public NodeConstructor newBlank()
	{
		return (Shape) Shape.getNewInstance(
				Helper.obtainInput(getAllOptions(), "Shape class", false));
	}

	@Override
	public void addChildObject(NodeConstructor childObject)
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public String defaultXmlTag()
	{
		return XmlLabel.compartmentShape;
	}
	
	/**
	 * \brief Initialise from an XML element.
	 * 
	 * <p>Note that all subclasses of Shape use this for initialisation,
	 * except for Dimensionless.</p>
	 * 
	 * @param xmlNode
	 */
	// TODO remove once ModelNode, etc is working
	public void init(Element xmlElem)
	{
		NodeList childNodes;
		Element childElem;
		String str;
		/* Set up the dimensions. */
		DimName dimName;
		Dimension dim;
		childNodes = XmlHandler.getAll(xmlElem, XmlLabel.shapeDimension);
		ResCalc rC;
		
		for ( int i = 0; i < childNodes.getLength(); i++ )
		{
			childElem = (Element) childNodes.item(i);
			try
			{
				str = XmlHandler.obtainAttribute(childElem,
												XmlLabel.nameAttribute);
				dimName = DimName.valueOf(str);
				dim = this.getDimension(dimName);
				dim.init(childElem);
				
				/* calculate length from dimension extremes */
				double length = dim.getLength();
				
				/* fetch target resolution (or use length as default) */
				str = XmlHandler.gatherAttribute(childElem,
						XmlLabel.targetResolutionAttribute);
				double tRes = length; 
				if ( str != "" )
					tRes = Double.valueOf(str);
				
				/* init resolution calculators */
				rC = new ResolutionCalculator.UniformResolution();
				rC.init(tRes, length);
				this.setDimensionResolution(dimName, rC);
			}
			catch (IllegalArgumentException e)
			{
				Log.out(Tier.CRITICAL, "Warning: input Dimension not "
						+ "recognised by shape " + this.getClass().getName()
						+ ", use: " + Helper.enumToString(DimName.class));
			}
		}
		
		/* Set up any other boundaries. */
		Boundary aBoundary;
		childNodes = XmlHandler.getAll(xmlElem, XmlLabel.dimensionBoundary);
		for ( int i = 0; i < childNodes.getLength(); i++ )
		{
			childElem = (Element) childNodes.item(i);
			str = childElem.getAttribute(XmlLabel.classAttribute);
			aBoundary = (Boundary) Boundary.getNewInstance(str);
			aBoundary.init(childElem);
			this.addOtherBoundary(aBoundary);
		}
	}
	
	@Override
	public String getXml()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
	
	
	public SpatialGrid getNewGrid()
	{
		return new SpatialGrid(this);
	}
		
	public abstract double[][][] getNewArray(double initialValue);
	
	/**
	 * \brief Convert a spatial position from global (Cartesian) coordinates to
	 * a new vector in the local coordinate scheme. 
	 * 
	 * @param cartesian
	 * @return
	 * @see #getGlobalLocation(double[])
	 */
	protected abstract double[] getLocalPosition(double[] cartesian);
	
	/**
	 * \brief TODO
	 * 
	 * @param local
	 * @return
	 */
	protected abstract double[] getGlobalLocation(double[] local);
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/

	/**
	 * \brief Return the number of "true" dimensions this shape has.
	 * 
	 * <p>Note that even 0-, 1- and 2-dimensional shapes will have a nonzero 
	 * thickness on their "missing" dimension(s).</p>
	 * 
	 * @return {@code int} number of significant dimensions for this shape.
	 */
	public int getNumberOfDimensions()
	{
		int out = 0;
		for ( Dimension dim : this._dimensions.values() )
			if ( dim.isSignificant() )
				out++;
		return out;
	}
	
	/**
	 * @param dimension The name of the dimension requested.
	 * @return The {@code Dimension} object.
	 */
	public Dimension getDimension(DimName dimension)
	{
		return this._dimensions.get(dimension);
	}
	
	/**
	 * @return The set of dimension names for this {@code Shape}.
	 */
	// TODO change to significant dimensions only?
	public Set<DimName> getDimensionNames()
	{
		return this._dimensions.keySet();
	}
	
	/**
	 * \brief Finds the index of the dimension name given.
	 * 
	 * <p>For example, in a cuboid, {@code Z} has index {@code 2}.</p>
	 * 
	 * @param dimension DimName of a dimension thought to be in this
	 * {@code Shape}.
	 * @return Index of the dimension, if present; {@code -1}, if not.
	 */
	protected int getDimensionIndex(DimName dimension)
	{
		int out = 0;
		for ( DimName d : this._dimensions.keySet() )
		{
			if ( d == dimension )
				return out;
			out++;
		}
		return -1;
	}
	
	/**
	 * \brief Find the name for the dimension with the given <b>index</b>.
	 * 
	 * @param index Index of the dimension required.
	 * @return Name of the dimension required.
	 */
	protected DimName getDimensionName(int index)
	{
		int counter = 0;
		for ( DimName d : this._dimensions.keySet() )
		{
			if ( counter == index )
				return d;
			counter++;
		}
		return null;
	}
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimensionName {@code String} name of the dimension. Lower/upper
	 * case is irrelevant.
	 */
	public void makeCyclic(String dimensionName)
	{
		this.makeCyclic(DimName.valueOf(dimensionName.toUpperCase()));
	}
	
	/**
	 * \brief Set the first <b>n</b> dimensions as significant.
	 * 
	 * <p>This method is safer that setting dimensions directly.</p>
	 * 
	 * @param n Number of dimensions to make significant.
	 */
	protected void setSignificant(int n)
	{
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( i >= n )
				return;
			dim.setSignificant();
			// TODO need to clarify setting boundaries optional/required
			i++;
		}
	}
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimension {@code DimName} enumeration of the dimension.
	 */
	public void makeCyclic(DimName dimension)
	{
		this.getDimension(dimension).setCyclic();
	}
	
	/**
	 * \brief Set a boundary of a given dimension.
	 * 
	 * @param dimension Name of the dimension to use.
	 * @param index Index of the extreme of this dimension to set the boundary
	 * to: should be 0 or 1. See {@code Boundary} for more information.
	 * @param bndry The {@code Boundary} to set.
	 */
	public void setBoundary(DimName dimension, int index, Boundary bndry)
	{
		this.getDimension(dimension).setBoundary(bndry, index);
	}
	
	/**
	 * \brief Gets the side lengths of only the significant dimensions.
	 * 
	 * @param lengths {@code double} array of significant side lengths.
	 * @see #getSideLengths()
	 */
	public double[] getDimensionLengths()
	{
		double[] out = new double[this._dimensions.size()];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			out[i] = dim.getLength();
			i++;
		}
		return out;
	}
	
	/**
	 * \brief Set the dimension lengths of this shape.
	 * 
	 * <p><b>Note</b>: If <b>lengths</b> has more than elements than this shape
	 * has dimensions, the extra elements will be ignored. If <b>lengths</b> 
	 * has fewer elements than this shape has dimensions, the remaining 
	 * dimensions will be given length zero.</p>
	 * 
	 * @param lengths {@code double} array of dimension lengths.
	 */
	public void setDimensionLengths(double[] lengths)
	{
		int i = 0;
		Dimension dim;
		for ( DimName d : this._dimensions.keySet() )
		{
			dim = this.getDimension(d);
			dim.setLength(( i < lengths.length ) ? lengths[i] : 0.0);
			i++;
		}
	}
	
	/**
	 * \brief Try to initialise a resolution calculator from storage, for a
	 * dimension that is dependent on another.
	 * 
	 * @param dName Name of the dimension to try.
	 */
	protected void trySetDimRes(DimName dName)
	{
		ResCalc rC = this._rcStorage.get(dName);
		if ( rC != null )
			this.setDimensionResolution(dName, rC);
	}
	
	/**
	 * \brief Set the resolution calculator for the given dimension, taking
	 * care to resolve any dependencies on other dimensions.
	 * 
	 * @param dName The name of the dimension to set for.
	 * @param resC A resolution calculator.
	 */
	public abstract void setDimensionResolution(DimName dName, ResCalc resC);
	
	/**
	 * \brief Get the Resolution Calculator for the given dimension, at the
	 * given coordinate.
	 * 
	 * @param coord Voxel coordinate.
	 * @param dim Dimension index (e.g., for a cuboid: X = 0, Y = 1, Z = 2).
	 * @return The relevant Resolution Calculator.
	 */
	protected abstract ResCalc getResolutionCalculator(int[] coord, int dim);
	
	/*************************************************************************
	 * LOCATIONS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param pos Current location (overwritten).
	 * @param dimN Name of the {@code Dimension} along which to move.
	 * @param dist Distance to move (can be negative).
	 */
	public void moveAlongDimension(double[] pos, DimName dimN, double dist)
	{
		if ( ! this._dimensions.keySet().contains(dimN) )
		{
			// TODO safety
		}
		double[] local = this.getLocalPosition(pos);
		if ( dimN.isAngular() )
		{
			double radius = local[this.getDimensionIndex(R)];
			if ( radius == 0.0 )
				return;
			double angle = dist/radius;
			this.moveAlongDim(local, dimN, angle);
		}
		else
			this.moveAlongDim(local, dimN, dist);
		pos = this.getGlobalLocation(local);
	}
	
	/**
	 * \brief Internal helper method for the
	 * {@link #moveAlongDimension(double[], DimName, double)} method.
	 * 
	 * @param loc Current location (overwritten).
	 * @param dimN Name of the {@code Dimension} along which to move.
	 * @param dist Distance to move (can be negative).
	 */
	protected void moveAlongDim(double[] loc, DimName dimN, double dist)
	{
		int dimIndex = this.getDimensionIndex(dimN);
		loc[dimIndex] += dist;
	}
	
	/**
	 * \brief Get a random location on a boundary of this shape.
	 * 
	 * @param dimN Name of the dimension used to find the boundary.
	 * @param extreme Index of the extreme of this dimension - must be 0 or 1.
	 * @return 
	 */
	public double[] getRandomLocationOnBoundary(DimName dimN, int extreme)
	{
		int nDim = this.getNumberOfDimensions();
		double[] out = new double[nDim];
		int i = 0;
		for ( DimName d : this._dimensions.keySet() )
		{
			Dimension dim = this.getDimension(d);
			if ( d.equals(dimN) )
				out[i] = dim.getExtreme(extreme);
			else
				out[i] = dim.getRandomInside();
			i++;
			if ( i >= nDim )
				break;
		}
		return this.getGlobalLocation(out);
	}
	
	/*************************************************************************
	 * SURFACES
	 ************************************************************************/
	
	/**
	 * \brief Set up this {@code Shape}'s surfaces.
	 * 
	 * <p>Surfaces are used by {@code Agent}s in collision detection.</p>
	 */
	public abstract void setSurfaces();
	
	/**
	 * \brief Set a flat surface at each extreme of the given dimension.
	 * 
	 * @param aDimName The name of the dimension required.
	 */
	protected void setPlanarSurfaces(DimName aDimName)
	{
		Dimension dim = this.getDimension(aDimName);
		/* Safety. */
		if ( dim == null )
			throw new IllegalArgumentException("Dimension not recognised");
		/* Cyclic behaviour is handled elsewhere. */
		if ( dim.isCyclic() )
			return;
		/*
		 * Create planar surfaces at each extreme.
		 */
		int index = this.getDimensionIndex(aDimName);
		double[] normal = Vector.zerosDbl( this.getNumberOfDimensions() );
		Plane p;
		/* The minimum extreme. */
		normal[index] = 1.0;
		p = new Plane( Vector.copy(normal), dim.getExtreme(0) );
		this._surfaces.put(p, dim.getBoundary(0));
		/* The maximum extreme. */
		normal[index] = -1.0;
		p = new Plane( Vector.copy(normal), - dim.getExtreme(1) );
		this._surfaces.put(p, dim.getBoundary(1));
	}
	
	/**
	 * @return The set of {@code Surface}s for this {@code Shape}.
	 */
	public Collection<Surface> getSurfaces()
	{
		return this._surfaces.keySet();
	}
	
	public Map<Surface, Boundary> getSurfaceBounds()
	{
		return this._surfaces;
	}
	
	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/
	
	/**
	 * \brief Add the given {@code Boundary} to this {@code Shape}'s list of
	 * "other" boundaries, i.e. those not associated with a {@code Dimension}.
	 * 
	 * @param aBoundary {@code Boundary} object.
	 */
	public void addOtherBoundary(Boundary aBoundary)
	{
		this._otherBoundaries.add(aBoundary);
	}
	
	/**
	 * @return Collection of connected boundaries.
	 */
	public Collection<Boundary> getAllBoundaries()
	{
		Collection<Boundary> out = new LinkedList<Boundary>();
		for ( Dimension d : this._dimensions.values() )
			for ( Boundary b : d.getBoundaries() )
				if ( b != null )
					out.add(b);
		for ( Boundary b : this._otherBoundaries )
			out.add(b);
		return out;
	}
	
	
	/**
	 * @return List of boundaries that need a partner boundary, but no not have
	 * one set.
	 */
	public Collection<Boundary> getDisconnectedBoundaries()
	{
		Collection<Boundary> out = this.getAllBoundaries();
		out.removeIf(b -> { return ! b.needsPartner();});
		return out;
	}
	
	/**
	 * @return Collection of all boundaries that do not belong to a dimension.
	 */
	public Collection<Boundary> getOtherBoundaries()
	{
		return this._otherBoundaries;
	}
	
	/**
	 * \brief Check if a given location is inside this shape.
	 * 
	 * @param location A spatial location in global coordinates.
	 * @return True if it is inside this shape, false if it is outside.
	 */
	public boolean isInside(double[] location)
	{
		double[] position = this.getLocalPosition(location);
		int nDim = location.length;
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( ! dim.isInside(position[i]) )
				return false;
			if ( ++i >= nDim )
				break;
		}
		return true;
	}
	
	/**
	 * \brief Force the given location to be inside this shape.
	 * 
	 * @param location A spatial location in global coordinates.
	 */
	public void applyBoundaries(double[] location)
	{
		double[] position = this.getLocalPosition(location);
		int nDim = location.length;
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			position[i] = dim.getInside(position[i]);
			if ( ++i >= nDim )
				break;
		}
		Vector.copyTo(location, this.getGlobalLocation(position));
	}
	
	/**
	 * \brief Find all neighbouring points in space that would  
	 * 
	 * <p>For use by the R-Tree.</p>
	 * 
	 * @param location A spatial location in global coordinates.
	 * @return List of nearby spatial locations, in global coordinates, that 
	 * map to <b>location</b> under cyclic transformation.
	 */
	public LinkedList<double[]> getCyclicPoints(double[] location)
	{
		/*
		 * Find all the cyclic points in local coordinates.
		 */
		double[] position = this.getLocalPosition(location);
		LinkedList<double[]> localPoints = new LinkedList<double[]>();
		localPoints.add(position);
		LinkedList<double[]> temp = new LinkedList<double[]>();
		double[] newPoint;
		int nDim = location.length;
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( dim.isCyclic() )
			{
				// TODO We don't need these in an angular dimension with 2 * pi
				for ( double[] loc : localPoints )
				{
					/* Add the point below. */
					newPoint = Vector.copy(loc);
					newPoint[i] -= dim.getLength();
					temp.add(newPoint);
					/* Add the point above. */
					newPoint = Vector.copy(loc);
					newPoint[i] += dim.getLength();
					temp.add(newPoint);
				}
				/* Transfer all from temp to out. */
				localPoints.addAll(temp);
				temp.clear();
			}
			/* Increment the dimension iterator, even if this isn't cyclic. */
			if ( ++i >= nDim )
				break;
		}
		/* Convert everything back into global coordinates and return. */
		LinkedList<double[]> out = new LinkedList<double[]>();
		for ( double[] p : localPoints )
			out.add(this.getGlobalLocation(p));
		return out;
	}
	
	/**
	 * \brief Get the smallest distance between two points, once cyclic
	 * dimensions are accounted for.
	 * 
	 * <p><b>a</b> - <b>b</b>, i.e. the vector from <b>b</b> to <b>a</b>.</p>
	 * 
	 * @param a A spatial location in global coordinates.
	 * @param b A spatial location in global coordinates.
	 * @return The smallest distance between them.
	 */
	public double[] getMinDifference(double[] a, double[] b)
	{
		// TODO safety with vector length & number of dimensions
		// TOD check this is the right approach in polar geometries
		Vector.checkLengths(a, b);
		double[] aLocal = this.getLocalPosition(a);
		double[] bLocal = this.getLocalPosition(b);
		int nDim = a.length;
		double[] diffLocal = new double[nDim];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			diffLocal[i] = dim.getShortest(aLocal[i], bLocal[i]);
			if ( ++i >= nDim )
				break;
		}
		return this.getGlobalLocation(diffLocal);
	}
	
	/*************************************************************************
	 * VOXELS
	 ************************************************************************/
	
	/**
	 * \brief Find the coordinates of the voxel that encloses the given
	 * <b>location</b>.
	 * 
	 * @param location Continuous location within the shape.
	 * @return Discrete coordinates within this grid.
	 */
	public int[] getCoords(double[] loc)
	{
		return getCoords(loc, null);
	}
	
	/**
	 * \brief Transforms a given location into array-coordinates and 
	 * computes sub-coordinates inside the grid element if inside != null. 
	 * 
	 * @param loc - a location in simulated space.
	 * @param inside - array to write sub-coordinates into, can be null.
	 * @return - the array coordinates corresponding to location loc.
	 */
	public int[] getCoords(double[] loc, double[] inside)
	{
		int[] coord = new int[3];
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			coord[dim] = rC.getVoxelIndex(loc[dim]);
			if ( inside != null )
			{
				inside[dim] = loc[dim] - 
								rC.getCumulativeResolution(coord[dim] - 1);
			}
		}
		return coord;
	}
	
	/**
	 * \brief For a voxel given by its coordinates, find the global location
	 * of a point inside it and write this location to <b>destination</b>.
	 * 
	 * <p>All elements of the vector <b>inside</b> should be in the range
	 * [0,1]. For example, if all are 0.5 then this method will find the centre
	 * of the voxel.</p>
	 * 
	 * @param destination Vector that will be overwritten with the result.
	 * @param coords Discrete coordinates of a voxel in this shape.
	 * @param inside Relative position inside the voxel.
	 */
	public void locationTo(double[] destination, int[] coord, double[] inside)
	{
		Vector.checkLengths(inside, coord);
		Vector.copyTo(destination, inside);
		int nDim = getNumberOfDimensions();
		ResCalc rC;
		for ( int dim = 0; dim < nDim; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] *= rC.getResolution(coord[dim]);
			destination[dim] += rC.getCumulativeResolution(coord[dim] - 1);
		}
	}
	
	/**
	 * \brief For a voxel given by its coordinates, find the global location
	 * of a point inside it.
	 * 
	 * <p>All elements of the vector <b>inside</b> should be in the range
	 * [0,1]. For example, if all are 0.5 then this method will find the centre
	 * of the voxel.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel in this shape.
	 * @param inside Relative position inside the voxel.
	 * @return Location in global coordinates.
	 */
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] destination = new double[coord.length];
		this.locationTo(destination, coord, inside);
		return destination;
	}
	
	/**
	 * \brief Find the location of the lower corner of the voxel specified by
	 * the given coordinates and write this location to <b>destination</b>.
	 * 
	 * @param destination Vector that will be overwritten with the result.
	 * @param coords Discrete coordinates of a voxel in this shape.
	 */
	public void voxelOriginTo(double[] destination, int[] coord)
	{
		this.locationTo(destination, coord, VOXEL_ORIGIN_HELPER);
	}
	
	/**
	 * \brief Find the location of the lower corner of the voxel specified by
	 * the given coordinates.
	 * 
	 * @param coords Discrete coordinates of a voxel in this shape.
	 * @return Continuous location of the lower corner of this voxel.
	 */
	public double[] getVoxelOrigin(int[] coord)
	{
		return this.getLocation(coord, VOXEL_ORIGIN_HELPER);
	}
	
	/**
	 * \brief Find the location of the centre of the voxel specified by the
	 * given coordinates, and write this to <b>destination</b>.
	 * 
	 * @param destination Vector that will be overwritten with the result.
	 * @param coords Discrete coordinates of a voxel in this shape.
	 */
	public void voxelCentreTo(double[] destination, int[] coord)
	{
		this.locationTo(destination, coord, VOXEL_CENTRE_HELPER);
	}
	
	/**
	 * \brief Find the location of the centre of the voxel specified by the
	 * given coordinates.
	 * 
	 * @param coords Discrete coordinates of a voxel in this shape.
	 * @return Continuous location of the centre of this voxel.
	 */
	public double[] getVoxelCentre(int[] coord)
	{
		return this.getLocation(coord, VOXEL_CENTRE_HELPER);
	}
	
	/**
	 * \brief Get the corner farthest from the origin of the voxel specified,
	 * and write this location to <b>destination</b>.
	 * 
	 * @param destination Vector that will be overwritten with the result.
	 * @param coords Discrete coordinates of a voxel in this shape.
	 */
	public void voxelUpperCornerTo(double[] destination, int[] coord)
	{
		this.locationTo(destination, coord, VOXEL_All_ONE_HELPER);
	}
	
	/**
	 * \brief Get the corner farthest from the origin of the voxel specified. 
	 * 
	 * @param coords Discrete coordinates of a voxel in this shape.
	 * @return Continuous location of the corner of this voxel that is furthest
	 * from its origin.
	 */
	protected double[] getVoxelUpperCorner(int[] coord)
	{
		return this.getLocation(coord, VOXEL_All_ONE_HELPER);
	}
	
	/**
	 * \brief Get the side lengths of the voxel given by the <b>coord</b>, and
	 * write the result into <b>destination</b>.
	 * 
	 * @param destination Vector that will be overwritten with the result.
	 * @param coords Discrete coordinates of a voxel in this shape.
	 */
	public void getVoxelSideLengthsTo(double[] destination, int[] coord)
	{
		ResCalc rC;
		for ( int dim = 0; dim < getNumberOfDimensions(); dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] = rC.getResolution(coord[dim]);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param coord
	 * @param dimName
	 * @return
	 */
	protected WhereAmI whereIs(int[] coord, DimName dimName)
	{
		Dimension dim = this.getDimension(dimName);
		int index = this.getDimensionIndex(dimName);
		if ( coord[index] < 0 )
			if ( dim.isBoundaryDefined(0) )
				return WhereAmI.DEFINED;
			else
				return WhereAmI.UNDEFINED;
		int nVox = this.getResolutionCalculator(coord, index).getNVoxel();
		if ( coord[index] >= nVox )
			if ( dim.isBoundaryDefined(1) )
				return WhereAmI.DEFINED;
			else
				return WhereAmI.UNDEFINED;
		return WhereAmI.INSIDE;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dimName
	 * @return
	 */
	protected WhereAmI whereIsNhb(DimName dimName)
	{
		return this.whereIs(this._currentNeighbor, dimName);
	}
	
	protected WhereAmI whereIsNhb()
	{
		this._whereIsNbh = INSIDE;
		WhereAmI where;
		for ( DimName dim : this._dimensions.keySet() )
		{
			where = this.whereIsNhb(dim);
			if ( where == UNDEFINED )
				return (this._whereIsNbh = UNDEFINED);
			if ( this._whereIsNbh == INSIDE && where == DEFINED )
				this._whereIsNbh = DEFINED;
		}
		return this._whereIsNbh;
	}
	
	/**
	 * \brief Calculate the greatest potential flux between neighboring voxels.
	 * 
	 * <p>This is useful in estimating the timestep of a PDE solver.</p>
	 * 
	 * <p>Units: area<sup>-1</sup>.</p>
	 * 
	 * @return Greatest potential flux between neighboring voxels.
	 */
	public double getMaxFluxPotential()
	{
		if ( this._maxFluxPotentl == null )
			this.calcMaxFluxPotential();
		return this._maxFluxPotentl;
	}
	
	/**
	 * \brief Helper method to calculate the maximum flux potential.
	 * 
	 * <p>This value will not change unless the resolutions do.</p>
	 */
	private void calcMaxFluxPotential()
	{
		/*
		 * Store the two iterators, in case we're in the middle of an
		 * iteration.
		 */
		int[] storeIter = null;
		int[] storeNbh = null;
		if ( this._currentCoord != null )
			storeIter = Vector.copy(this._currentCoord);
		if ( this._currentNeighbor != null )
			storeNbh = Vector.copy(this._currentNeighbor);
		/*
		 * Loop over all voxels, finding the greatest flux potential.
		 */
		double volume;
		double max;
		double temp = 0.0;
		this._maxFluxPotentl = Double.NEGATIVE_INFINITY;
		for ( this.resetIterator(); this.isIteratorValid(); this.iteratorNext())
		{
			volume = this.getVoxelVolume(this._currentCoord);
			Log.out(Tier.BULK, "Coord "+Vector.toString(this._currentCoord)+
					" has volume "+volume);
			max = 0.0;
			for ( this.resetNbhIter();
						this.isNbhIteratorValid(); this.nbhIteratorNext() )
			{
				temp = this.nbhCurrSharedArea() / this.nbhCurrDistance();
				Log.out(Tier.BULK, "   nbh "+
						Vector.toString(this._currentNeighbor)+
						" has shared area "+this.nbhCurrSharedArea()+
						" and distance "+this.nbhCurrDistance());
				max = Math.max(max, temp);
			}
			
			this._maxFluxPotentl = Math.max(this._maxFluxPotentl, max/volume);
		}
		/*
		 * Put the iterators back to their stored values.
		 */
		this._currentCoord = (storeIter == null) ? null:Vector.copy(storeIter);
		this._currentNeighbor = (storeNbh==null) ? null:Vector.copy(storeNbh);
	}
	
	/**
	 * \brief Calculate the volume of the voxel specified by the given
	 * coordinates.
	 * 
	 * @param coord Discrete coordinates of a voxel on this grid.
	 * @return Volume of this voxel.
	 */
	public abstract double getVoxelVolume(int[] coord);

	/**
	 * \brief Get the number of voxels in each dimension for the given
	 * coordinates.
	 * 
	 * @param destination Integer vector to write the result into.
	 * @param coords Discrete coordinates of a voxel on this shape.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	protected void nVoxelTo(int[] destination, int[] coords)
	{
		Vector.checkLengths(destination, coords);
		int n = Math.min(coords.length, 3);
		ResCalc rC;
		for ( int dim = 0; dim < n; dim++ )
		{
			rC = this.getResolutionCalculator(coords, dim);
			destination[dim] = rC.getNVoxel();
		}
	}
	
	/*************************************************************************
	 * SUBVOXEL POINTS
	 ************************************************************************/
	
	/**
	 * \brief List of sub-voxel points at the current coordinate.
	 * 
	 * <p>Useful for distributing agent-mediated reactions over the grid.</p>
	 * 
	 * @param targetRes
	 * @return
	 */
	public List<SubvoxelPoint> getCurrentSubvoxelPoints(double targetRes)
	{
		if ( targetRes <= 0.0 )
			throw new IllegalArgumentException("Target resolution must be > 0");
		/* 
		 * Initialise the list and add a point at the origin.
		 */
		ArrayList<SubvoxelPoint> out = new ArrayList<SubvoxelPoint>();
		SubvoxelPoint current = new SubvoxelPoint();
		out.add(current);
		/*
		 * For each dimension, work out how many new points are needed and get
		 * these for each point already in the list.
		 */
		int nP, nCurrent;
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			// TODO Rob[17Feb2016]: This will need improving for polar grids...
			// I think maybe would should introduce a subclass of Dimension for
			// angular dimensions.
			rC = this.getResolutionCalculator(this._currentCoord, dim);
			nP = (int) (rC.getResolution(this._currentCoord[dim])/targetRes);
			nCurrent = out.size();
			for ( int j = 0; j < nCurrent; j++ )
			{
				current = out.get(j);
				/* Shift this point up by half a sub-resolution. */
				current.internalLocation[dim] += (0.5/nP);
				/* Now add extra points at sub-resolution distances. */
				for ( double i = 1.0; i < nP; i++ )
					out.add(current.getNeighbor(dim, i/nP));
			}
		}
		/* Now find the real locations and scale the volumes. */
		// TODO this probably needs to be slightly different in polar grids
		// to be completely accurate
		double volume = this.getVoxelVolume(this._currentCoord) / out.size();
		for ( SubvoxelPoint aSgP : out )
		{
			aSgP.realLocation = this.getLocation(this._currentCoord,
													aSgP.internalLocation);
			aSgP.volume = volume;
		}
		return out;
	}
	
	/*************************************************************************
	 * COORDINATE ITERATOR
	 ************************************************************************/
	
	/**
	 * \brief Return the coordinate iterator to its initial state.
	 * 
	 * @return The value of the coordinate iterator.
	 */
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
		{
			this._currentCoord = Vector.zerosInt(this._dimensions.size());
			this._currentNVoxel = Vector.zerosInt(this._dimensions.size());
		}
		else
			Vector.reset(this._currentCoord);
		this.updateCurrentNVoxel();	
		return this._currentCoord;
	}
	
	/**
	 * \brief Determine whether the current coordinate of the iterator is
	 * outside the grid in the dimension specified.
	 * 
	 * @param dim Index of the dimension to look at.
	 * @return Whether the coordinate iterator is inside (false) or outside
	 * (true) the grid along this dimension.
	 */
	protected boolean iteratorExceeds(int dim)
	{
		return this._currentCoord[dim] >= this._currentNVoxel[dim];
	}
	
	/**
	 * \brief Check if the current coordinate of the internal iterator is
	 * valid.
	 * 
	 * @return True if is valid, false if it is invalid.
	 */
	public boolean isIteratorValid()
	{
		int nDim = this.getNumberOfDimensions();
		for ( int dim = 0; dim < nDim; dim++ )
			if ( this.iteratorExceeds(dim) )
				return false;
		return true;
	}
	
	public int[] iteratorCurrent()
	{
		return _currentCoord;
	}
	
	/**
	 * \brief Step the coordinate iterator forward once.
	 * 
	 * @return The new value of the coordinate iterator.
	 */
	public int[] iteratorNext()
	{
		/*
		 * We have to step through last dimension first, because we use jagged 
		 * arrays in the PolarGrids.
		 */
		_currentCoord[2]++;
		if ( this.iteratorExceeds(2) )
		{
			_currentCoord[2] = 0;
			_currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				_currentCoord[1] = 0;
				_currentCoord[0]++;
			}
		}
		if ( this.isIteratorValid() )
			this.updateCurrentNVoxel();
		return _currentCoord;
	}
	
	/**
	 * \brief Get the number of voxels in each dimension for the current
	 * coordinates.
	 * 
	 * <p>For Cartesian shapes the value of <b>coords</b> will be
	 * irrelevant, but it will make a difference in Polar shapes.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel on this shape.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	public int[] updateCurrentNVoxel()
	{
		if ( this._currentNVoxel == null )
			this._currentNVoxel = Vector.zerosInt(3);
		if ( this._currentCoord == null )
			this.resetIterator();
		this.nVoxelTo(this._currentNVoxel, this._currentCoord);
		return this._currentNVoxel;
	}
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/
	
	/**
	 * \brief Reset the neighbor iterator.
	 * 
	 * <p>Typically used just after the coordinate iterator has moved.</p>
	 * 
	 * @return The current neighbor coordinate.
	 */
	public int[] resetNbhIterator()
	{
		/* Set the neighbor to the current coordinate. */
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			Vector.copyTo(this._currentNeighbor, this._currentCoord);
		/* Find the first neighbor by shape type. */
		this.resetNbhIter();
		/* Return the current neighbour coordinate. */
		return this._currentNeighbor;
	}
	
	public int[] nbhIteratorCurrent()
	{
		return _currentNeighbor;
	}
	
	/**
	 * \brief Check if the neighbor iterator takes a valid coordinate.
	 * 
	 * <p>Valid coordinates are either inside the array, or on a defined
	 * boundary.</p>
	 * 
	 * @return {@code boolean true} if it is valid, {@code false} if it is not.
	 */
	public boolean isNbhIteratorValid()
	{
		return (this._whereIsNbh == INSIDE) || (this._whereIsNbh == DEFINED);
	}
	
	/**
	 * \brief Check if the neighbor iterator takes a coordinate inside the
	 * array.
	 * 
	 * @return {@code boolean true} if it is inside, {@code false} if it is
	 * on a boundary (defined or undefined).
	 */
	public boolean isNhbIteratorInside()
	{
		return (this._whereIsNbh == INSIDE);
	}
	
	/**
	 * \brief Check if the neighbor iterator is on a defined boundary.
	 * 
	 * @return The respective boundary or null if the nbh iterator is inside.
	 */
	public Boundary nbhIteratorOutside()
	{
		if ( this._whereIsNbh == DEFINED )
		{
			return getDimension(this._nbhDimName)
					.getBoundaries()[this._nbhDirection];
		}
		return null;
	}
	
	/**
	 * \brief Calculates the overlap between the current iterator voxel and the
	 * neighbor voxel, in the dimension given by <b>index</b>.
	 * 
	 * @param index Index of the required dimension.
	 * @return Overlap length between the two voxels.
	 */
	protected double getNbhSharedLength(int index)
	{
		ResCalc rC;
		double curMin, curMax, nbhMin, nbhMax;
		/* Current voxel of the main iterator. */
		rC = this.getResolutionCalculator(this._currentCoord, index);
		curMin = rC.getCumulativeResolution(this._currentCoord[index] - 1);
		curMax = rC.getCumulativeResolution(this._currentCoord[index]);
		/* Current voxel of the neighbor iterator. */
		rC = this.getResolutionCalculator(this._currentNeighbor, index);
		nbhMin = rC.getCumulativeResolution(this._currentNeighbor[index] - 1);
		nbhMax = rC.getCumulativeResolution(this._currentNeighbor[index]);
		/* Find the overlap. */
		return ExtraMath.overlap(curMin, curMax, nbhMin, nbhMax);
	}
	
	/**
	 * \brief Transform the coordinates of the neighbor iterator, in the
	 * current neighbor direction, so that that they lie within the array.
	 * 
	 * <p>This should be reversed using {@link #untransformNbhCyclic()}.</p>
	 */
	protected void transformNbhCyclic()
	{
		Dimension dim = getDimension(this._nbhDimName);
		if ( (this._whereIsNbh == DEFINED) && dim.isCyclic() )
		{
			int dimIdx = this.getDimensionIndex(this._nbhDimName);
			int nVoxel = this.getResolutionCalculator(
					this._currentCoord, dimIdx).getNVoxel();
			if ( this._nbhDirection == 0 )
			{
				/* Direction 0: the neighbor wraps below, to the highest. */
				this._currentNeighbor[dimIdx] = nVoxel - 1;
			}
			else
			{
				/* Direction 1: the neighbor wraps above, to zero. */
				this._currentNeighbor[dimIdx] = 0;
			}
		}
	}
	
	/**
	 * \brief Reverses the transformation of {@link #transformNbhCyclic()},
	 * putting the coordinates of the neighbor iterator that wrap around a
	 * cyclic dimension back where they were.
	 */
	protected void untransformNbhCyclic()
	{
		Dimension dim = this.getDimension(this._nbhDimName);
		if ( (this._whereIsNbh == DEFINED) && dim.isCyclic() )
		{
			int dimIdx = this.getDimensionIndex(this._nbhDimName);
			if ( this._nbhDirection == 0 )
			{
				/* Direction 0: the neighbor should be below. */
				this._currentNeighbor[dimIdx] = this._currentCoord[dimIdx] - 1;
			}
			else
			{
				/* Direction 1: the neighbor should be above. */
				this._currentNeighbor[dimIdx] = this._currentCoord[dimIdx] + 1;
			}
		}
	}
	
	/**
	 * \brief Move the neighbor iterator to the current coordinate, 
	 * and make the index at <b>dim</b> one less.
	 * If successful, sets <b>_nbhDirection</b> to 1 (lower than current coord)
	 * If Outside, but on defined boundary, sets <b>_nbhOnBoundary</b> to true.
	 * 
	 * @return {@code boolean} reporting whether this is valid.
	 */
	protected boolean moveNbhToMinus(DimName dim)
	{
		int index = this.getDimensionIndex(dim);
		/* Move to the coordinate just belong the current one. */
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[index]--;
		/* Check that this coordinate is acceptable. */
		WhereAmI where = this.whereIsNhb(dim);
		this._whereIsNbh = where;
		this._nbhDirection = 0;
		this._nbhDimName = dim;
		return (where != UNDEFINED);
	}
	
	/**
	 * \brief Try to increase the neighbor iterator from the minus-side of the
	 * current coordinate to the plus-side.
	 * 
	 * <p>For use on linear dimensions (X, Y, Z, R) and not on angular ones
	 * (THETA, PHI).</p>
	 * 
	 * @param dim Index of the dimension to move in.
	 * @return Whether the increase was successful (true) or a failure (false).
	 */
	protected boolean nbhJumpOverCurrent(DimName dim)
	{
		int index = this.getDimensionIndex(dim);
		this.updateCurrentNVoxel();
		this._whereIsNbh = this.whereIsNhb(dim);
		/* Check we are behind the current coordinate. */
		if ( this._currentNeighbor[index] < this._currentCoord[index] )
		{
			boolean bMaxDef = this.getDimension(dim).isBoundaryDefined(1);
			/* Check there is space on the other side. */
			if ( this._whereIsNbh == INSIDE || bMaxDef )
			{
				/* Jump and report success. */
				this._nbhDirection = 1;
				this._whereIsNbh = this.whereIsNhb(dim);
				this._currentNeighbor[index] = this._currentCoord[index] + 1;
				return true;
			}
		}
		/* Report failure. */
		// TODO is it appropriate to use a meaningless direction here?
		this._nbhDirection = -1;
		this._whereIsNbh = this.whereIsNhb(dim);
		return false;
	}
	
	/**
	 * \brief Helper method for resetting the neighbor iterator, to be
	 * implemented by subclasses.
	 */
	protected abstract void resetNbhIter();
	
	/**
	 * @return The next neighbor iterator coordinate.
	 */
	public abstract int[] nbhIteratorNext();
	
	/**
	 * @return The centre-centre distance between the current iterator voxel
	 * and the neighbor voxel.
	 */
	public abstract double nbhCurrDistance();
	
	/**
	 * @return The shared surface area between the current iterator voxel
	 * and the neighbor voxel.
	 */
	public abstract double nbhCurrSharedArea();
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		/* Check all dimensions are ready. */
		for ( Dimension dim : this._dimensions.values() )
			if ( ! dim.isReadyForLaunch() )
			{
				// TODO
				return false;
			}
		/* If there are any other boundaries, check these are ready. */
		for ( Boundary bound : this._otherBoundaries )
			if ( ! bound.isReadyForLaunch() )
				return false;
		/* All checks passed: ready to launch. */
		return true;
	}
	
	/*************************************************************************
	 * XML-ABLE
	 ************************************************************************/
	
	public static Shape getNewInstance(String className)
	{
		return (Shape) XMLable.getNewInstance(className, "shape.ShapeLibrary$");
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	// TODO remove all this once ModelNode, etc is working
	
	public List<InputSetter> getRequiredInputs()
	{
		List<InputSetter> out = new LinkedList<InputSetter>();
		for ( DimName d : this._dimensions.keySet() )
			out.add(new DimensionMaker(d, Requirement.EXACTLY_ONE, this));
		// TODO other boundaries
		return out;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
									ShapeLibrary.class.getDeclaredClasses());
	}
	
	public void acceptInput(String name, Object input)
	{
		if ( input instanceof Dimension )
		{
			Dimension dim = (Dimension) input;
			DimName dN = DimName.valueOf(name);
			this._dimensions.put(dN, dim);
		}
	}
	
	public static class ShapeMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = 1486068039985317593L;

		public ShapeMaker(Requirement req, IsSubmodel target)
		{
			super(XmlLabel.compartmentShape, req, target);
		}
		
		@Override
		public void doAction(ActionEvent e)
		{
			// TODO do safety properly
			String shapeName;
			if ( e == null )
				shapeName = "";
			else
				shapeName = e.getActionCommand();
			this.addSubmodel(Shape.getNewInstance(shapeName));
		}
		
		@Override
		public Object getOptions()
		{
			return Shape.getAllOptions();
		}
	}
}
