/**
 * 
 */
package shape;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import boundary.BoundaryConnected;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import shape.ShapeConventions.DimName;
import surface.Plane;
import surface.Point;
import surface.Ball;
import surface.Surface;
import utility.Helper;
/**
 * \brief Abstract class for all shape objects.
 * 
 * <p>These are typically used by {@code Compartment}s; cell shapes are
 * currently described using {@code Body} and {@code Surface} objects.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Shape implements CanPrelaunchCheck, IsSubmodel, XMLable
{
	/**
	 * Ordered dictionary of dimensions for this shape.
	 */
	protected LinkedHashMap<DimName, Dimension> _dimensions = 
								 	   new LinkedHashMap<DimName, Dimension>();
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected Collection<Boundary> _otherBoundaries = 
													new LinkedList<Boundary>();
	
	/**
	 * Surface Object for collision detection methods
	 */
	protected Collection<Surface> _surfaces = new LinkedList<Surface>();
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public Shape()
	{
		
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>Note that all subclasses of Shape use this for initialisation,
	 * except for Dimensionless.</p>
	 * 
	 * @param xmlNode
	 */
	public void init(Element xmlElem)
	{
		NodeList childNodes;
		Element childElem;
		String str;
		/* Set up the dimensions. */
		DimName dimName;
		childNodes = XmlHandler.getAll(xmlElem, "dimension");
		for ( int i = 0; i < childNodes.getLength(); i++ )
		{
			childElem = (Element) childNodes.item(i);
			try
			{
				str = XmlHandler.obtainAttribute(childElem,
												XmlLabel.nameAttribute);
				dimName = DimName.valueOf(str);
				this.getDimension(dimName).init(childElem);
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
		childNodes = XmlHandler.getAll(xmlElem, "boundary");
		for ( int i = 0; i < childNodes.getLength(); i++ )
		{
			childElem = (Element) childNodes.item(i);
			str = childElem.getAttribute(XmlLabel.classAttribute);
			aBoundary = (Boundary) Boundary.getNewInstance(str);
			aBoundary.init(childElem);
			this.addOtherBoundary(aBoundary);
		}
	}
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
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
	 * \brief TODO
	 * 
	 * @param dimension
	 * @return
	 */
	public Dimension getDimension(DimName dimension)
	{
		return this._dimensions.get(dimension);
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
	
	protected Dimension getDimensionSafe(DimName dimension)
	{
		if ( this._dimensions.containsKey(dimension) )
		{
			Dimension dim = this._dimensions.get(dimension);
			if ( dim == null )
				this._dimensions.put(dimension, (dim = new Dimension()));
			return dim;
		}
		else
		{
			// TODO safety
			return this.getDimension(dimension);
		}
	}
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimension {@code DimName} enumeration of the dimension.
	 */
	public void makeCyclic(DimName dimension)
	{
		this.getDimensionSafe(dimension).setCyclic();
	}
	
	public void setBoundary(DimName dimension, int index, Boundary bndry)
	{
		this.getDimensionSafe(dimension).setBoundary(bndry, index);
	}
	
	
	/**
	 * \brief Set the minimum and maximum boundaries for the given dimension.
	 * 
	 * @param dimensionName {@code String} name of the dimension.
	 * @param bndry
	 * @param setMin
	 */
	public void setBoundary(String dimension, Boundary bndry, int index)
	{
		this.setBoundary(DimName.valueOf(dimension), index, bndry);
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
	 * <p>NOTE: If <b>lengths</b> has more than elements than this shape has
	 * dimensions, the extra elements will be ignored. If <b>lengths</b> has
	 * fewer elements than this shape has dimensions, the remaining dimensions
	 * will be given length zero.</p>
	 * 
	 * @param lengths {@code double} array of dimension lengths.
	 */
	public void setDimensionLengths(double[] lengths)
	{
		int i = 0;
		Dimension dim;
		for ( DimName d : this._dimensions.keySet() )
		{
			dim = this.getDimensionSafe(d);
			dim.setLength(( i < lengths.length ) ? lengths[i] : 0.0);
			i++;
		}
	}
	
	/**
	 * TODO Method to replace setSurfaces()
	 * 
	 */
	public abstract void setSurfs();
	
	protected void setPlanarSurfaces(DimName aDimName)
	{
		Dimension dim = this.getDimension(aDimName);
		// TODO safety if dim == null
		if ( dim.isCyclic() )
			return;
		int index = this.getDimensionIndex(aDimName);
		double[] normal = Vector.zerosDbl( this.getNumberOfDimensions() );
		Plane p;
		/* The minimum extreme. */
		normal[index] = 1.0;
		p = new Plane( Vector.copy(normal), dim.getExtreme(0) );
		this._surfaces.add( p );
		/* The maximum extreme. */
		normal[index] = -1.0;
		p = new Plane( Vector.copy(normal), - dim.getExtreme(1) );
		this._surfaces.add( p );
	}
	
	/**
	 * Set the collision surface object
	 * 
	 * NOTE: this would be a lot cleaner if the dimensions would be aware of 
	 * their dimension (returning their own surface objects), but I could only 
	 * found these information as the keyset of the hashmap.
	 * FIXME Shape currently does not allow to set a starting point of the
	 * domain.. this would be nice and necessary if we want to have an R minimum
	 * hard boundary.. Imagine a biofilm growing on a particle.
	 * TODO: cylinders, PHI, THETA planes, min surfaces that can be set.  
	 */
	public void setSurfaces()
	{
		for( DimName d : this._dimensions.keySet())
		{
			if(! this._dimensions.get(d)._isCyclic)
			{
				double[] normal = Vector.zerosDbl( this._dimensions.size() );
				double[] min = Vector.zerosDbl( this._dimensions.size() );
				switch (d)
				{
				case X:
					normal[0] = 1.0;
					_surfaces.add( new Plane( Vector.copy(normal) , Vector.dotProduct( 
							normal, min)));
					normal[0] = -1.0;
					_surfaces.add( new Plane( Vector.copy(normal) , Vector.dotProduct(
							normal, this.getDimensionLengths() )));
					break;
				case Y:
					normal[1] = 1.0;
					_surfaces.add( new Plane( Vector.copy(normal) , Vector.dotProduct( 
							normal, min )));
					normal[1] = -1.0;
					_surfaces.add( new Plane( Vector.copy(normal) , Vector.dotProduct(
							normal , this.getDimensionLengths() )));
					break;
				case Z:
					normal[2] = 1.0;
					_surfaces.add( new Plane( Vector.copy(normal) , Vector.dotProduct( 
							normal, min)));
					normal[2] = -1.0;
					_surfaces.add( new Plane( Vector.copy(normal) , Vector.dotProduct( 
							normal , this.getDimensionLengths() )));
					/*
					 * FIXME the Cylinder has two dimensions but the second is
					 * stored as Z does we have to make an exception for it...
					 * or better, make the shape aware of its shape (enum?)
					 * allows to create a switch case based on shape rather than
					 * dimensions
					 */
					break;
				case R:
					/*
					 * FIXME Shape currently does not allow to set a minimum R
					 * thus we cannot set an Rmin hard boundary
					 * TODO does the shape know whether it is of sphere type
					 * or cylinder type otherwise we have to check whether
					 * both R and PHI/THETA?/X?/Y?/Z? exists before we start the 
					 * switch case. This should be stored somewhere since always 
					 * reverse engineering this does not make a a lot of sense.
					 */
					Ball outbound = new Ball( new Point(min) ,
							this._dimensions.get(d).getLength() );
					outbound.bounding = true;
					_surfaces.add(outbound);
					break;
				case PHI:
					// TODO
					break;
				case THETA:
					// TODO
				}
			}
		}
	}
	
	/**
	 * returns the surfaces set for this shape
	 */
	public Collection<Surface> getSurfaces()
	{
		return this._surfaces;
	}
	
	/**
	 * \brief Return the number of "true" dimensions this shape has.
	 * 
	 * <p>Note that even 0-, 1- and 2-dimensional shapes may have a nonzero 
	 * thickness on their "missing" dimension(s).</p>
	 * 
	 * @return {@code int} number of dimensions for this shape.
	 */
	public int getNumberOfDimensions()
	{
		return this._dimensions.size();
	}
	
	public Set<DimName> getDimensionNames()
	{
		return this._dimensions.keySet();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract GridGetter gridGetter();
	
	protected abstract double[] getLocalPosition(double[] cartesian);
	
	protected abstract double[] getGlobalLocation(double[] local);
	
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
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<BoundaryConnected> getConnectedBoundaries()
	{
		LinkedList<BoundaryConnected> cB = new LinkedList<BoundaryConnected>();
		for ( Dimension dim : this._dimensions.values() )
				for ( Boundary b : dim.getBoundaries() )
					if ( b instanceof BoundaryConnected )
						cB.add((BoundaryConnected) b);
		for ( Boundary b : this._otherBoundaries )
			if ( b instanceof BoundaryConnected )
				cB.add((BoundaryConnected) b);
		return cB;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<Boundary> getOtherBoundaries()
	{
		return this._otherBoundaries;
	}
	
	/**
	 * \brief Check if a given location is inside this shape.
	 * 
	 * @param location 
	 * @return
	 */
	public boolean isInside(double[] location)
	{
		double[] position = this.getLocalPosition(location);
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( ! dim.isInside(position[i]) )
				return false;
			i++;
		}
		return true;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param location
	 */
	public void applyBoundaries(double[] location)
	{
		double[] position = this.getLocalPosition(location);
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			position[i] = dim.getInside(position[i]);
			i++;
		}
		Vector.copyTo(location, this.getGlobalLocation(position));
	}
	
	/**
	 * \brief Find all neighbouring points in space that would  
	 * 
	 * <p>For use by the R-Tree.</p>
	 * 
	 * @param location
	 * @return
	 */
	public LinkedList<double[]> getCyclicPoints(double[] location)
	{
		// TODO safety with vector length
		/*
		 * Find all the cyclic points in 
		 */
		double[] position = this.getLocalPosition(location);
		LinkedList<double[]> localPoints = new LinkedList<double[]>();
		localPoints.add(position);
		LinkedList<double[]> temp = new LinkedList<double[]>();
		double[] newPoint;
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
			i++;
		}
		/* Convert everything back into global coordinates and return. */
		LinkedList<double[]> out = new LinkedList<double[]>();
		for ( double[] p : localPoints )
			out.add(this.getGlobalLocation(p));
		return localPoints;
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p><b>a</b> - <b>b</b>, i.e. the vector from <b>b</b> to <b>a</b>.</p>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double[] getMinDifference(double[] a, double[] b)
	{
		// TODO safety with vector length & number of dimensions
		// TOD check this is the right approach in polar geometries
		Vector.checkLengths(a, b);
		double[] aLocal = this.getLocalPosition(a);
		double[] bLocal = this.getLocalPosition(b);
		double[] diffLocal = new double[a.length];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			diffLocal[i] = dim.getShortest(aLocal[i], bLocal[i]);
			i++;
		}
		return this.getGlobalLocation(diffLocal);
	}
	
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
	
	public LinkedHashMap<String, Class<?>> getParameters()
	{
		// TODO
		return new LinkedHashMap<String, Class<?>>();
	}
	
	public void setParameter(String name, String value)
	{
		/* No parameters to set. */
		//return true;
	}
	
	public List<SubmodelMaker> getSubmodelMakers()
	{
		// TODO
		return new LinkedList<SubmodelMaker>();
	}
	
	public static String[] getAllOptions()
	{
		Class<?>[] classNames = ShapeLibrary.class.getDeclaredClasses();
		int num = classNames.length;
		String[] out = new String[num];
		String str;
		int dollarIndex;
		for ( int i = 0; i < num; i++ )
		{
			str = classNames[i].getName();
			dollarIndex = str.indexOf("$");
			out[i] = str.substring(dollarIndex+1);
		}
		return out;
	}
}
