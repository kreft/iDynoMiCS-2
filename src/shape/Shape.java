package shape;

import static shape.Dimension.DimName.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import boundary.SpatialBoundary;
import boundary.WellMixedBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import generalInterfaces.CanPrelaunchCheck;
import instantiable.Instance;
import instantiable.Instantiable;
import linearAlgebra.Orientation;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import shape.Dimension.DimName;
import shape.ShapeConventions.SingleVoxel;
import shape.iterator.ShapeIterator;
import shape.resolution.ResolutionCalculator;
import shape.resolution.UniformResolution;
import shape.subvoxel.SubvoxelPoint;
import surface.Plane;
import surface.Surface;
import surface.collision.Collision;
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
 * <li>Grid & array construction</li>
 * <li>Dimensions</li>
 * <li>Point locations</li>
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
					CanPrelaunchCheck, Instantiable, Settable
{
	/**
	 * Ordered dictionary of dimensions for this shape.
	 * TODO switch to a Shape._dimensions a Dimension[3] paradigm
	 */
	protected LinkedHashMap<DimName, Dimension> _dimensions = 
									new LinkedHashMap<DimName, Dimension>();
	/**
	 * Saves resolution calculator specification for output xml 
	 */
	protected String resCal;
	/**
	 * Storage container for dimensions that this {@code Shape} is not yet
	 * ready to initialise.
	 */
	protected HashMap<DimName,ResolutionCalculator> _rcStorage = 
			new HashMap<DimName,ResolutionCalculator>();
	
	/**
	 * The greatest potential flux between neighboring voxels. Multiply by 
	 * diffusivity to become actual flux.
	 */
	protected Double _maxFluxPotentl = null;
	
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected Collection<Boundary> _otherBoundaries = 
			new LinkedList<Boundary>();
	
	protected ShapeIterator _it;
	
	/**
	 * TODO
	 */
	protected Collision _defaultCollision;
	/**
	 * 
	 */
	protected Integer _numSignificantDimension;
	/**
	 * Name of the dimension, whose extreme holds the surface for biofilm
	 * attachment. Set to null for volume based calculation of scaling factor.
	 */
	protected DimName _realDimExtremeName = null;
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
	/**
	 * Specifies the shape orientation.
	 */
	private Orientation _orientation;
	protected Settable _parentNode;
	private Element _currElement;
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	@Override
	public Module getModule()
	{
		/* Create a new Module */
		Module modelNode = new Module( XmlRef.compartmentShape, this );
		/* Set the requirement */
		modelNode.setRequirements( Requirements.EXACTLY_ONE );
		/* Add the attributes, note the Shape class is not editable thus set
		 * editable to false */
		modelNode.add( new Attribute(XmlRef.classAttribute, 
										this.getName(), null, false ) );
		/** Add resolution calculator specification  */
		modelNode.add( new Attribute(XmlRef.resolutionCalculator, 
				this.resCal, null, false ) );
		
		/* orientation node */
		if( !(Helper.isNullOrEmpty(this._orientation) || this._orientation.isNullVector()) )
			modelNode.add( this._orientation.getModule() );
		
		/* Add the child modules */
		for ( Dimension dim : this._dimensions.values() )
			if ( dim._isSignificant )
				modelNode.add( dim.getModule() );
		
		/* Add boundaries that are not asociated with a dimension */
		for ( Boundary bound : this._otherBoundaries)
		{
			if ( !bound.assistingBoundary() )
				modelNode.add( bound.getModule());
		}
		
		/* NOTE: no constructible child modules for this class thus no 
		 * addChildSpec */
		return modelNode;
	}


	@Override
	public String defaultXmlTag()
	{
		return XmlRef.compartmentShape;
	}
	
	/**
	 * \brief Initialise from an XML element.
	 * 
	 * <p>Note that all subclasses of Shape use this for initialisation,
	 * except for Dimensionless.</p>
	 * 
	 * @param xmlNode
	 */
	public void instantiate(Element xmlElem, Settable parent )
	{
		this._parentNode = parent;
		this._currElement = xmlElem;
		
		NodeList childNodes;
		Element childElem;
		String str;
		Double dbl;
		/* FIXME is this a correct implementation? This would create multiple
		 * layers of voxels if we have a small target resolution eg 0.5µm. 
		 * Shouldn't this be calculated from a target volume? or maybe just
		 * equal to the resolution so we always have 1 layer in the
		 * insignificant dimension. [Bas 27-11-2017] */
		double insignificantDimsLength = 1.0;
		dbl = XmlHandler.gatherDouble(xmlElem, "insignificantDimsLength");
		if ( dbl != null )
			insignificantDimsLength = dbl;
		/* Set up the dimensions. */
		Dimension dim;
		ResolutionCalculator rC;
		for ( DimName dimName : this.getDimensionNames() )
		{
			childElem = (Element) XmlHandler.getSpecific(xmlElem, 
					XmlRef.shapeDimension, XmlRef.nameAttribute, dimName.name());
			try
			{
				dim = this.getDimension(dimName);
				if ( dim._isSignificant )
				{
					dim.instantiate(childElem, this);
					resCal = XmlHandler.gatherAttribute(xmlElem, 
							XmlRef.resCalcClass);
						if ( resCal == null )	
							resCal = UniformResolution.class.getSimpleName();
					/* Initialise resolution calculators */
					rC = (ResolutionCalculator) Instance.getNew(xmlElem, this, 
							resCal );
					rC.setDimension(dim);
					rC.setResolution(dim._targetRes);
					this.setDimensionResolution(dimName, rC);
				}
				else
				{
					rC = new SingleVoxel(dim);
					rC.setResolution(insignificantDimsLength);
					this.setDimensionResolution(dimName, rC);
					dim.setLength(insignificantDimsLength);
				}
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
				Log.out(Tier.CRITICAL, "Warning: input Dimension not "
						+ "recognised by shape " + this.getClass().getName()
						+ ", use: " + Helper.enumToString(DimName.class));
			}
			this._defaultCollision = new Collision(this);	
		}
		this.setRealDimExtremeName();
		
		/* Set up any other boundaries. */
		Boundary aBoundary;
		childNodes = XmlHandler.getAll(xmlElem, XmlRef.dimensionBoundary);
		if ( childNodes != null )
		{
			for ( int i = 0; i < childNodes.getLength(); i++ )
			{
				childElem = (Element) childNodes.item(i);
				/* 
				 * Skip boundaries that are not direct children of the shape
				 * node (e.g. those wrapped in a dimension).
				 */
				if ( childElem.getParentNode() != xmlElem )
					continue;
				
				str = childElem.getAttribute(XmlRef.classAttribute);
				aBoundary = (Boundary) Instance.getNew(childElem, 
						this, str );
				this.addOtherBoundary(aBoundary);
			}
		}
		this.setSurfaces();
	}
	
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	/**
	 * @return Name of this shape.
	 */
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract double getTotalVolume();
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract double getTotalRealVolume();
	
	/**
	 * 
	 */
	public abstract void setTotalVolume( double volume );
	
	/**
	 * 
	 */
	public void setOrientation( Orientation orientation )
	{
		this._orientation = orientation;
	}
	
	
	public Orientation getOrientation()
	{
		return this._orientation;
	}
	
	public boolean isOriented()
	{
		return !this._orientation.isNullVector();
	}
	
	/* ***********************************************************************
	 * GRID & ARRAY CONSTRUCTION
	 * **********************************************************************/
	
	/**
	 * \brief Get a new array with the layout of voxels that follows this
	 * shape's discretisation scheme.
	 * 
	 * @param initialValue Fill every voxel with this value.
	 * @return New three-dimensional array of real numbers.
	 */
	public abstract double[][][] getNewArray(double initialValue);
	
	/* ***********************************************************************
	 * DIMENSIONS
	 * **********************************************************************/

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
		if ( this._numSignificantDimension != null )
			return this._numSignificantDimension;
		this._numSignificantDimension = 0;
		for ( Dimension dim : this._dimensions.values() )
			if ( dim.isSignificant() )
				this._numSignificantDimension++;
		return this._numSignificantDimension;
	}
	
	/**
	 * @return List of all significant dimensions.
	 */
	public List<Dimension> getSignificantDimensions()
	{
		LinkedList<Dimension> out = new LinkedList<Dimension>();
		for ( Dimension dim : this._dimensions.values() )
			if ( dim.isSignificant() )
				out.add(dim);
		return out;
	}
	
	/**
	 * @return List of the indices for each significant dimension.
	 */
	public List<Integer> getSignificantDimensionsIndices()
	{
		LinkedList<Integer> out = new LinkedList<Integer>();
		for ( Dimension dim : this._dimensions.values() )
			if ( dim.isSignificant() )
				out.add(this.getDimensionIndex(dim));
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
	public int getDimensionIndex(DimName dimension)
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
	 * \brief Finds the index of the dimension given.
	 * 
	 * @param dimension Dimension thought to be in this {@code Shape}.
	 * @return Index of the dimension, if present; {@code -1}, if not.
	 */
	public int getDimensionIndex(Dimension dimension)
	{
		int out = 0;
		for ( Dimension d : this._dimensions.values() )
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
	public DimName getDimensionName(int index)
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
	
	/*
	 * returns an array of booleans that indicate whether the dimensions are
	 * periodic in their natural order.
	 */
	public boolean[] getIsCyclicNaturalOrder()
	{
		boolean[] dims = new boolean[this.getNumberOfDimensions()];
		int i = 0;
		for (Dimension d : this.getSignificantDimensions())
			dims[i++] = d.isCyclic();
		return dims;
	}
	
	/*
	 * returns an array of booleans that indicate whether the dimensions are
	 * periodic in their natural order.
	 */
	public boolean[] getIsCyclicNaturalOrderIncludingVirtual()
	{
		boolean[] dims = new boolean[3];
		int i = 0;
		for (Dimension d : this.getSignificantDimensions())
			dims[i++] = d.isCyclic();
		return dims;
	}
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimensionName {@code String} name of the dimension. Lower/upper
	 * case is irrelevant.
	 */
	public void makeCyclic(String dimensionName)
	{
		Log.out(Tier.NORMAL, "Making dimension "+dimensionName+" cyclic");
		this.makeCyclic(DimName.valueOf(dimensionName.toUpperCase()));
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
	 * \brief Set the first <b>n</b> dimensions as significant.
	 * 
	 * <p>This method is safer than setting dimensions directly.</p>
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
	 * \brief Gets the side lengths of only the significant dimensions.
	 * 
	 * @param lengths {@code double} array of significant side lengths.
	 */
	public double[] getDimensionLengths()
	{
		double[] out = new double[this.getNumberOfDimensions()];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
			if ( dim.isSignificant() )
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
		ResolutionCalculator rC = this._rcStorage.get(dName);
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
	public abstract void setDimensionResolution(DimName dName, 
			ResolutionCalculator resC);
	
	/**
	 * \brief Get the Resolution Calculator for the given dimension, at the
	 * given coordinate.
	 * 
	 * @param coord Voxel coordinate.
	 * @param dim Dimension index (e.g., for a cuboid: X = 0, Y = 1, Z = 2).
	 * @return The relevant Resolution Calculator.
	 */
	public abstract ResolutionCalculator getResolutionCalculator(int[] coord, 
			int dim);
	
	/**
	 * \brief Gets the real lengths of only the significant dimensions.
	 * 
	 * @return lengths {@code double} array of significant side lengths.
	 */
	public double[] getRealLengths()
	{
		double[] out = new double[this.getNumberOfDimensions()];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
			if ( dim.isSignificant() )
			{
				out[i] = dim.getRealLength();
				i++;
			}
		return out;
	}
	
	/**
	 * \brief Gets the dimension name whose extreme holds the surface for 
	 * attachment
	 * 
	 * @return dimension name {@code DimName}.
	 */
	public DimName getRealDimExtremeName()
	{
		return this._realDimExtremeName;
	}
	
	/**
	 * \brief Set the dimension name which does not have realMax defined.
	 */
	public void setRealDimExtremeName()
	{
		Dimension dim;
		int sigDims = this.getNumberOfDimensions();
		if (sigDims == this._dimensions.size())
		{
			for ( DimName d : this._dimensions.keySet() )
			{
				Element childElem = (Element) XmlHandler.getSpecific(
						this._currElement, XmlRef.shapeDimension, 
						XmlRef.nameAttribute, d.name());
				Double dbl = XmlHandler.gatherDouble(childElem, 
						XmlRef.realMax);
				this._realDimExtremeName = dbl == null ? d : null;
			}
		}
		else
		{
			for ( DimName d : this._dimensions.keySet() )
			{
				dim = this.getDimension(d);
				if (dim.isSignificant())
					continue;
				this._realDimExtremeName = d;
			}
		}
	}
	
	/* ***********************************************************************
	 * POINT LOCATIONS
	 * **********************************************************************/

	/**
	 * \brief Convert a spatial position from global (Cartesian) coordinates to
	 * a new vector in the local coordinate scheme, writing the result into the
	 * destination vector given.
	 * 
	 * @param dest The destination vector that will be overwritten with the
	 * result.
	 * @param cartesian A point in space represented by a vector in global
	 * (Cartesian) coordinates.
	 */
	public abstract void getLocalPositionTo(
			double[] destination, double[] cartesian);
	
	/**
	 * \brief Convert a spatial position from global (Cartesian) coordinates to
	 * a new vector in the local coordinate scheme. 
	 * 
	 * @param cartesian A point in space represented by a vector in global
	 * (Cartesian) coordinates.
	 * @return The same point in space, converted to a vector in the local
	 * coordinate scheme.
	 * @see #getGlobalLocation(double[])
	 */
	public double[] getLocalPosition(double[] cartesian)
	{
		double[] out = new double[cartesian.length];
		this.getLocalPositionTo(out, cartesian);
		return out;
	}
	
	/**
	 * \brief Convert a spatial position from global (Cartesian) coordinates to
	 * the local coordinate scheme, overwriting the original vector with the
	 * result. 
	 * 
	 * @param cartesian A point in space represented by a vector in global
	 * (Cartesian) coordinates. Overwritten.
	 */
	public void getLocalPositionEquals(double[] cartesian)
	{
		this.getLocalPositionTo(cartesian, cartesian);
	}
	
	/**
	 * \brief Convert a spatial position from the local coordinate scheme to
	 * global (Cartesian) coordinates, writing the result into the destination
	 * vector given.
	 * 
	 * @param dest The destination vector that will be overwritten with the
	 * result.
	 * @param local A point in space represented by a vector in local
	 * coordinate scheme.
	 */
	public abstract void getGlobalLocationTo(double[] dest, double[] local);
	
	/**
	 * \brief Convert a spatial position from the local coordinate scheme to
	 * a new vector in global (Cartesian) coordinates.
	 * 
	 * @param local A point in space represented by a vector in local
	 * coordinate scheme.
	 * @return The same point in space, converted to a vector in the global
	 * (Cartesian) coordinates.
	 * @see #getLocalPosition(double[])
	 */
	public double[] getGlobalLocation(double[] local)
	{
		double[] out = new double[local.length];
		this.getGlobalLocationTo(out, local);
		return out;
	}
	
	/**
	 * \brief Convert a spatial position from the local coordinate scheme to
	 * global (Cartesian) coordinates, writing the result into the vector given.
	 * 
	 * @param local A point in space represented by a vector in local
	 * coordinate scheme (overwritten).
	 */
	public void getGlobalLocationEquals(double[] local)
	{
		this.getGlobalLocationTo(local, local);
	}
	
	/**
	 * 
	 */
	public double[] getVerifiedLocation(double[] loc)
	{
		if( isInside(loc))
			return loc;
		else
		{
			for( double[] d : getCyclicPoints(loc))
				if( isInside(d))
					return d;
			/* if no inside point is found there is an illegal boundary 
			 * intersection. */
			Log.out(Tier.CRITICAL, "invalid location lookup in " + 
					this.getClass().getSimpleName());
			return loc;
		}
				
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
			/* NOTE that the dim should not be cyclic if it is insignificant */ 
			if ( dim.isCyclic() )
			{
				/* NOTE that a full-length polar dimension is always cyclic */
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
	
	public double[] getNearestShadowPoint(double[] dynamicPos, 
			double[] staticPos)
	{
		/*
		 * FIXME think of something more robust
		 * NOTE this will only cause problems when the actual length of the
		 * agent is over 50% of the domain.
		 * 
		 * find the closest distance between the two mass points of the rod
		 * agent and assumes this is the correct length, preventing rods being
		 * stretched out over the entire domain
		 * 
		 * Here we assume that posB will stay fixed, so we are looking for a
		 * candidate position for the "A" end of the cylinder.
		 */
		List<double[]> cyclicPoints = this.getCyclicPoints(dynamicPos);
		double[] c = cyclicPoints.get(0);

		/* distance between the two mass points */
		double dist = Vector.distanceEuclid(staticPos, c);
		double dDist;
		/* 
		 * find the closest 'shadow' point, use the original point if all
		 * alternative point are further.
		 */
		for ( double[] d : cyclicPoints )
		{
			dDist = Vector.distanceEuclid( staticPos, d);
			if ( dDist < dist)
			{
				c = d;
				dist = dDist;
			}
		}

		return c;
	}
	
	/**
	 * Considering periodic boundaries return the mid-point on the shortest
	 * distance between two points, even if the shortest distance passes trough
	 * a periodic boundary.
	 * @param a position vector
	 * @param b position vector
	 * @return mid-point position vector considering the periodic boundaries of
	 * this shape.
	 */
	public double[] periodicMidPoint(double[] a, double[] b)
	{
		List<double[]> cyclicPoints = getCyclicPoints(a);
		double[] c = cyclicPoints.get(0);
		double dist = Vector.distanceEuclid(b, c);
		double dDist;
		for ( double[] d : cyclicPoints )
		{
			dDist = Vector.distanceEuclid( b, d);
			if ( dDist < dist)
			{
				c = d;
				dist = dDist;
			}
		}
		double[] midPos = Vector.midPoint(c, b);
		return this.getGlobalLocation(midPos);
	}
	
	/**
	 * \brief Get the smallest distance between two points, once cyclic
	 * dimensions are accounted for. Write the result into the destination
	 * vector given.
	 * 
	 * <p><b>a</b> - <b>b</b>, i.e. the vector from <b>b</b> to <b>a</b>.</p>
	 * 
	 * @param a A spatial location in global coordinates.
	 * @param b A spatial location in global coordinates.
	 * @return The smallest distance between them.
	 */
	public void getMinDifferenceVectorTo(double[] destination, double[] a, 
			double[] b)
	{
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( dim.isAngular() )
			{
				// FIXME If we want to use periodic boundaries in polar
				// coordinates the collision algorithm needs to be overhauled
				// to work with direction vector conversion since the angle
				// applied to the different cells is no longer identical
				destination[i] = a[i]-b[i];
			}
			else
			{
				destination[i] = dim.getShortest(a[i], b[i]);
			}
			if ( ++i >= a.length )
				break;
		}

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
	public double[] getMinDifferenceVector(double[] a, double[] b)
	{
		/* NOTE getMinDifferenceTo checks length, we do not have to do that 
		 * twice. */
		double[] out = new double[a.length];
		this.getMinDifferenceVectorTo(out, a, b);
		return out;
	}
	
	/**
	 * \brief Move a point position along a dimension by a distance.
	 * 
	 * @param pos Vector representing a point position in space (overwritten).
	 * @param dimN Name of the {@code Dimension} along which to move.
	 * @param dist Distance to move (can be negative).
	 */
	public void moveAlongDimension(double[] pos, DimName dimN, double dist)
	{
		this.getLocalPositionEquals(pos);
		if ( dimN.isAngular() )
		{
			double radius = pos[this.getDimensionIndex(R)];
			if ( radius == 0.0 )
				return;
			double angle = dist/radius;
			this.moveAlongDim(pos, dimN, angle);
		}
		else
			this.moveAlongDim(pos, dimN, dist);
		this.getGlobalLocationEquals(pos);
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

	/**
	 * \brief Force the given location to be inside this shape.
	 * FIXME broken for polar shapes
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
	
	/* ***********************************************************************
	 * SURFACES
	 * **********************************************************************/
	
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
	protected void setPlanarSurface(DimName aDimName)
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
		p.init(_defaultCollision);
		//this._surfaces.put(p, dim.getBoundary(0));
		dim.setSurface(p, 0);
		/* The maximum extreme. */
		normal[index] = -1.0;
		p = new Plane( Vector.copy(normal), - dim.getExtreme(1) );
		p.init(_defaultCollision);
		//this._surfaces.put(p, dim.getBoundary(1));
		dim.setSurface(p, 1);
	}
	
	/**
	 * @return The set of {@code Surface}s for this {@code Shape}.
	 * FIXME this method does not work for polar shapes
	 */
	public Collection<Surface> getSurfaces()
	{
		Collection<Surface> out = new LinkedList<Surface>();
		for ( Dimension dim : this._dimensions.values() )
			for ( int extreme = 0; extreme < 2; extreme++ )
				if ( dim.isSurfaceDefined(extreme) )
					out.add(dim.getSurface(extreme));
		return out;
	}
	
	/**
	 * \brief Get the spatial boundary associated with the given surface.
	 * 
	 * @param surface Surface object on one of this shape's boundaries.
	 * @return The {@code SpatialBoundary} object linked to this surface.
	 */
	public SpatialBoundary getBoundary(Surface surface)
	{
		for ( Dimension dim : this._dimensions.values() )
			for ( int extreme = 0; extreme < 2; extreme++ )
				if ( dim.getSurface(extreme) == surface )
					return dim.getBoundary(extreme);
		return null;
	}
	
	/**
	 * \brief Get the surface associated with the given spatial boundary.
	 * 
	 * @param boundary Boundary object on one of this shape's surfaces.
	 * @return The {@code Surface} object linked to this boundary.
	 */
	public Surface getSurface(SpatialBoundary boundary)
	{
		DimName dimName = boundary.getDimName();
		int extreme = boundary.getExtreme();
		Dimension dim = this.getDimension(dimName);
		Surface out = dim.getSurface(extreme);
		return out;
	}
	
	/* ***********************************************************************
	 * BOUNDARIES
	 * **********************************************************************/

	/**
	 * \brief Set a boundary of a given dimension.
	 * 
	 * @param dimension Name of the dimension to use.
	 * @param index Index of the extreme of this dimension to set the boundary
	 * to: should be 0 or 1. See {@code Boundary} for more information.
	 * @param bndry The {@code Boundary} to set.
	 */
	public void setBoundary(DimName dimension, int index, SpatialBoundary bndry)
	{
		this.getDimension(dimension).setBoundary(bndry, index);
	}
	
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
	 * @return Collection of boundaries.
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
	 * @return Collection of all spatial boundaries.
	 */
	public Collection<SpatialBoundary> getSpatialBoundaries()
	{
		Collection<SpatialBoundary> out = new LinkedList<SpatialBoundary>();
		for ( Dimension d : this._dimensions.values() )
			for ( SpatialBoundary b : d.getBoundaries() )
				if ( b != null )
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
	 * @return Collection of spatial boundaries that have a well-mixed approach
	 * to them.
	 */
	public Collection<WellMixedBoundary> getWellMixedBoundaries()
	{
		Collection<WellMixedBoundary> out = new LinkedList<WellMixedBoundary>();
		for ( Dimension d : this._dimensions.values() )
			for ( SpatialBoundary b : d.getBoundaries() )
				if ( b != null && b.needsToUpdateWellMixed() )
					out.add((WellMixedBoundary) b);
		return out;
	}
	
	/**
	 * @return Collection of all boundaries that do not belong to a dimension.
	 */
	public Collection<Boundary> getNonSpatialBoundaries()
	{
		return this._otherBoundaries;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dimN
	 * @param extreme
	 * @return
	 */
	public abstract double getBoundarySurfaceArea(DimName dimN, int extreme);
	
	/**
	 * \brief TODO
	 * 
	 * @param dimN
	 * @param extreme
	 * @return
	 */
	public abstract double getRealSurfaceArea(DimName dimN, int extreme);
	
	/* ***********************************************************************
	 * VOXELS
	 * **********************************************************************/
	
	public abstract int getTotalNumberOfVoxels();
	
	/**
	 * \brief Find the coordinates of the voxel that encloses the given
	 * <b>location</b>.
	 * 
	 * @param location Continuous location within the shape.
	 * @return Discrete coordinates within this grid.
	 */
	public int[] getCoords(double[] loc)
	{
		return this.getCoords(loc, null);
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
		ResolutionCalculator rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			/* if the location comes from a 1D or 2D system set the coordinate
			 * index for additional dimensions to 0. */
			if( loc.length < dim+1)
				coord[dim] = 0;
			else
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
		ResolutionCalculator rC;
		for ( int dim = 0; dim < nDim; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] *= rC.getResolution();
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
	 * @param coord Discrete coordinates of a voxel in this shape.
	 */
	public void voxelCentreTo(double[] destination, int[] coord)
	{
		this.locationTo(destination, coord, VOXEL_CENTRE_HELPER);
	}
	
	/**
	 * \brief Find the location of the centre of the voxel specified by the
	 * given coordinates.
	 * 
	 * @param coord Discrete coordinates of a voxel in this shape.
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
	public double[] getVoxelUpperCorner(int[] coord)
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
		ResolutionCalculator rC;
		for ( int dim = 0; dim < getNumberOfDimensions(); dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] = rC.getResolution();
		}
	}
	
	/* ***********************************************************************
	 * SHAPE AND VOXEL PROPERTIES 
	 * **********************************************************************/
	
	/**
	 * \brief Calculate the greatest potential flux between neighboring voxels.
	 * 
	 * <p>This is useful in estimating the timestep of a PDE solver.</p>
	 * 
	 * <p>Units: area<sup>-1</sup> = length<sup>-2</sup>.</p>
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
		 * Store the current iterator and use a temporary one for this method.
		 */
		ShapeIterator storedIterator = this._it;
		this._it = this.getNewIterator();
		
		/*
		 * Loop over all voxels, finding the greatest flux potential.
		 */
		double volume;
		double max;
		double temp = 0.0;
		this._maxFluxPotentl = Double.NEGATIVE_INFINITY;
		for ( this._it.resetIterator(); 
				this._it.isIteratorValid(); this._it.iteratorNext() )
		{
			volume = this.getVoxelVolume(this._it.iteratorCurrent());
			max = 0.0;
			for ( this._it.resetNbhIterator(); this._it.isNbhIteratorValid(); 
					this._it.nbhIteratorNext() )
			{
				temp = this.nhbCurrSharedArea() / this.nhbCurrDistance();
				max = Math.max(max, temp);
			}
			this._maxFluxPotentl = Math.max(this._maxFluxPotentl, max/volume);
		}
		/*
		 * Replace the temporary iterator with the stored one.
		 */
		this._it = storedIterator;
	}
	
	
	/**
	 * \brief Calculate the volume of the voxel specified by the given
	 * coordinates.
	 * 
	 * @param coord Discrete coordinates of a voxel on this grid.
	 * @return Volume of this voxel.
	 */
	public double getVoxelVolume(int[] coord)
	{
		double[] origin = this.getVoxelOrigin(coord);
		double[] upper = this.getVoxelUpperCorner(coord);
		return getVoxelVolume(origin, upper);
	}
	
	public abstract double getVoxelVolume(double[] origin, double[] uppers);

	/**
	 * @return The volume of the current iterator voxel.
	 */
	public double getCurrVoxelVolume()
	{
		return this.getVoxelVolume(this._it.iteratorCurrent());
	}
	
	public double currentDistanceFromBoundary(DimName dimN, int extreme)
	{
		Dimension dim = this.getDimension(dimN);
		int dimIndex = this.getDimensionIndex(dimN);
		int[] currentCoord = this._it.iteratorCurrent();
		ResolutionCalculator rC = this.getResolutionCalculator(currentCoord, 
				dimIndex);
		/*
		 * Get the position at the centre of the current voxel.
		 */
		double distance = rC.getPosition(currentCoord[dimIndex], 0.5);
		/*
		 * Correct for the position of the extreme.
		 */
		if ( extreme == 0 )
			distance -= dim.getExtreme(extreme);
		else
			distance = dim.getExtreme(extreme) - distance;
		/*
		 * If this is an angular dimension, convert from distance in radians to
		 * distance in length units. To do this conversion, multiply by the
		 * radius.
		 */
		if ( dimN.isAngular() )
		{
			dimIndex = this.getDimensionIndex(R);
			rC = this.getResolutionCalculator(currentCoord, dimIndex);
			distance *= rC.getPosition(currentCoord[dimIndex], 0.5);
		}
		return distance;
	}
	
	/**
	 * \brief Find the distance between the centre of the current iterator
	 * voxel and the centre of the current neighbor voxel, along the current
	 * neighbor dimension.
	 * 
	 * <p>If the neighbor is on a defined boundary, this instead returns the
	 * resolution of the current iterator voxel in the relevant dimension.</p>
	 * 
	 * @return The centre-centre distance between the current iterator voxel
	 * and the neighbor voxel.
	 */
	public double nhbCurrDistance()
	{
		int[] currentCoord = this._it.iteratorCurrent();
		int[] currentNeighbor = this._it.nbhIteratorCurrent();
		DimName nhbDimName = this._it.currentNhbDimName();
		int i = this.getDimensionIndex(nhbDimName);
		ResolutionCalculator rC = this.getResolutionCalculator(currentCoord, i);
		double out = rC.getResolution();
		/* 
		 * If the neighbor is inside the array, use the mean resolution.
		 * 
		 * If the neighbor is on a defined boundary, use the current coord's
		 * resolution.
		 */
		if ( this._it.isNbhIteratorInside() )
		{
			rC = this.getResolutionCalculator(currentNeighbor, i);
			out += rC.getResolution();
			out *= 0.5;
		}
		if ( this._it.isNbhIteratorValid() )
		{
			/* If the dimension is angular, find the arc length. */
			if ( nhbDimName.isAngular() )
			{
				i = this.getDimensionIndex(R);
				rC = this.getResolutionCalculator(currentCoord, i);
				double radius = rC.getPosition(currentCoord[i],0.5);
				out *= radius;
			}
			return out;
		}
		/* If the neighbor is on an undefined boundary, return infinite
			distance (this should never happen!) */
		if ( Log.shouldWrite(Tier.CRITICAL) )
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() + 
					"encountered undefined distance!");
		return Double.POSITIVE_INFINITY;
	}
	
	
	/**
	 * @return The shared surface area between the current iterator voxel
	 * and the neighbor voxel.
	 */
	public abstract double nhbCurrSharedArea();
	
	/* ***********************************************************************
	 * ITERATORS
	 * **********************************************************************/

	/**
	 * \brief Get a new iterator for this shape. The type of iterator will
	 * depend on the type of shape.
	 * 
	 * @param strideLength The stride length of the iterator: 1 for normal, 2
	 * for red-black, etc. Must be greater than zero.
	 * @return New shape iterator object.
	 * @see #getNewIterator()
	 */
	protected abstract ShapeIterator getNewIterator(int strideLength);

	/**
	 * \brief Get a new iterator for this shape. The type of iterator will
	 * depend on the type of shape.
	 * 
	 * @return New shape iterator object.
	 * @see #getNewIterator(int)
	 */
	public ShapeIterator getNewIterator()
	{
		return this.getNewIterator(1);
	}

	/**
	 * \brief Replace this shape's iterator with a new one.
	 * 
	 * @param strideLength The stride length of the iterator: 1 for normal, 2
	 * for red-black, etc. Must be greater than zero.
	 */
	public void setNewIterator(int strideLength)
	{
		this._it = this.getNewIterator(strideLength);
	}
	
	public int[] resetIterator()
	{
		return this._it.resetIterator();
	}
	
	public int[] iteratorCurrent()
	{
		return this._it.iteratorCurrent();
	}
	
	public int[] iteratorNext()
	{
		return this._it.iteratorNext();
	}
	
	public boolean isIteratorValid()
	{
		return this._it.isIteratorValid();
	}
	
	public int[] resetNbhIterator()
	{
		return this._it.resetNbhIterator();
	}
	
	public int[] nbhIteratorCurrent()
	{
		return this._it.nbhIteratorCurrent();
	}
	
	public int[] nbhIteratorNext()
	{
		return this._it.nbhIteratorNext();
	}
	
	public boolean isNbhIteratorValid(){
		return this._it.isNbhIteratorValid();
	}
	
	public boolean isNbhIteratorInside(){
		return this._it.isNbhIteratorInside();
	}
	
	public SpatialBoundary nbhIteratorOutside(){
		return this._it.nbhIteratorOutside();
	}

	/* ***********************************************************************
	 * SUBVOXEL POINTS
	 * **********************************************************************/
	
	/**
	 * \brief Compile a collection of sub-voxel points at the current 
	 * iterator coordinate.
	 * 
	 * <p>Useful for distributing agent-mediated reactions over the grid.</p>
	 * 
	 * @param targetRes Target resolution for the distance between sub-voxel
	 * points.
	 * @return Collection of sub-voxel points.
	 */
	public Collection<SubvoxelPoint> getCurrentSubvoxelPoints(double targetRes)
	{
		if ( targetRes <= 0.0 )
			throw new IllegalArgumentException("Target resolution must be > 0");
		/* 
		 * Initialise the list and get voxel extremes.
		 */
		List<SubvoxelPoint> out = new ArrayList<SubvoxelPoint>();
		double[] orig = getVoxelOrigin(this._it.iteratorCurrent());
		double[] upper = getVoxelUpperCorner(this._it.iteratorCurrent());
		
		/* Create a shape of the same runtime type as the current shape */
		Shape sub = null;
		try {
			sub = (Shape) this.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* set the dimension resolutions */
		for ( int dimIndex = 0; dimIndex < 3; dimIndex++ )
		{
			DimName dimName = this.getDimensionName(dimIndex);
			Dimension dimension = sub.getDimension(dimName);
			dimension.setExtremes(orig[dimIndex], upper[dimIndex]);
			UniformResolution resCalc = new UniformResolution(dimension);
			/* convert arc length to angle for angular dimensions */
			resCalc.setResolution(dimName.isAngular() ? targetRes / 
					upper[getDimensionIndex(DimName.R)] : targetRes);
			sub.setDimensionResolution(dimName, resCalc);
		}
		
		/* Loop over the shape and create new subvoxel points with 
		 * internal location, real location and volume set */
		for (int[] cur = sub.resetIterator(); sub.isIteratorValid(); 
				cur = sub.iteratorNext())
		{
			SubvoxelPoint sp = new SubvoxelPoint();
			double[] local = sub.getVoxelCentre(cur);
			sp.realLocation = sub.getGlobalLocation(local);
			/* this will compute the internal location and write it into 
			 * sp.internalLocation */
			this.getCoords(local, sp.internalLocation);
			sp.volume = sub.getCurrVoxelVolume();
			out.add(sp);
		}
		
		return out;
	}
	
	/* ***********************************************************************
	 * MULTIGRID CONSTRUCTION
	 * **********************************************************************/
	
	public abstract boolean canGenerateCoarserMultigridLayer();
	
	public abstract Shape generateCoarserMultigridLayer();
	
	/* ***********************************************************************
	 * PRE-LAUNCH CHECK
	 * **********************************************************************/
	
	@Override
	public boolean isReadyForLaunch()
	{
		/* Check all dimensions are ready. */
		for ( Dimension dim : this._dimensions.values() )
			if ( ! dim.isReadyForLaunch() )
				return false;
		/* If there are any other boundaries, check these are ready. */
		for ( Boundary bound : this._otherBoundaries )
			if ( ! bound.isReadyForLaunch() )
				return false;
		/* All checks passed: ready to launch. */
		return true;
	}
	
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
				ShapeLibrary.class.getDeclaredClasses());
	}

	public Collision getCollision()
	{
		return this._defaultCollision;
	}
	
	public void setParent(Settable parent)
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}