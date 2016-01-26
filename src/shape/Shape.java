/**
 * 
 */
package shape;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import boundary.Boundary;
import boundary.BoundaryConnected;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.GridBoundary.GridMethod;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeDimension.*;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Shape implements CanPrelaunchCheck, XMLable
{
	/**
	 * Ordered dictionary of dimensions for this shape.
	 */
	protected LinkedHashMap<DimName, Dimension> _dimensions = 
									new LinkedHashMap<DimName, Dimension>();
	/**
	 * A list of boundary sides that must be specified.
	 */
	protected Collection<BoundarySide> _requiredBoundarySides;
	
	/**
	 * Directory of boundaries that are linked to a specific side. There can
	 * be only one boundary for each boundary side here.
	 */
	protected HashMap<BoundarySide, Boundary> _sideBoundaries;
	
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected Collection<Boundary> _otherBoundaries;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public Shape()
	{
		this._requiredBoundarySides = new LinkedList<BoundarySide>();
		this._sideBoundaries = new HashMap<BoundarySide, Boundary>();
		this._otherBoundaries = new LinkedList<Boundary>();
	}
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimensionName {@code String} name of the dimension.
	 */
	public void makeCyclic(String dimensionName)
	{
		this.makeCyclic(DimName.valueOf(dimensionName));
	}
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimension {@code DimName} enumeration of the dimension.
	 */
	public void makeCyclic(DimName dimension)
	{
		if ( this._dimensions.containsKey(dimension) )
		{
			Dimension dim = this._dimensions.get(dimension);
			if ( dim == null )
				this._dimensions.put(dimension, new CyclicDimension());
			else if ( ! (dim instanceof CyclicDimension) )
			{
				// TODO safety
			}
		}
		else
		{
			// TODO safety
		}
	}
	
	public void setBoundary(DimName dimension, Boundary bndry, boolean setMin)
	{
		if ( this._dimensions.containsKey(dimension) )
		{
			Dimension dim = this._dimensions.get(dimension);
			BoundedDimension bDim;
			if ( dim == null )
			{
				bDim = new BoundedDimension();
				this._dimensions.put(dimension, bDim);
			}
			else if ( dim instanceof BoundedDimension )
				bDim = (BoundedDimension) dim;
			else
			{
				// TODO safety
				return;
			}
			if ( setMin )
				bDim.setMinBoundary(bndry);
			else
				bDim.setMaxBoundary(bndry);
		}
		else
		{
			// TODO safety
		}
	}
	
	
	/**
	 * \brief Set the minimum and maximum boundaries for the given dimension.
	 * 
	 * @param dimensionName {@code String} name of the dimension.
	 * @param bndry
	 * @param setMin
	 */
	public void setBoundary(String dimension, Boundary bndry, boolean setMin)
	{
		this.setBoundary(DimName.valueOf(dimension), bndry, setMin);
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
		int maxDim = Math.min(this._dimensions.size(), lengths.length);
		Iterator<Dimension> iter = this._dimensions.values().iterator();
		int i = 0;
		while ( iter.hasNext() && i < maxDim )
			iter.next().setLength(lengths[i]);
		while ( iter.hasNext() )
			iter.next().setLength(0.0);
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
	
	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param aSide
	 * @param aBoundary
	 */
	public void addBoundary(BoundarySide aSide, Boundary aBoundary)
	{
		/* If this boundary is required, we can now take it off the list. */
		if ( this._requiredBoundarySides.contains(aSide) )
			this._requiredBoundarySides.remove(aSide);
		
		// TODO Rob [14Jan2015]: throw an error/warning if a side boundary is
		// being overwritten?
		// TODO Rob [14Jan2015]: separate lists for internal & connection
		// boundaries? 
		if ( BoundarySide.isSideBoundary(aSide) )
			this._sideBoundaries.put(aSide, aBoundary);
		else
			this._otherBoundaries.add(aBoundary);	
	}
	
	public Set<BoundarySide> getBoundarySides()
	{
		return this._sideBoundaries.keySet();
	}
	
	public GridMethod getGridMethod(BoundarySide aSide, String soluteName)
	{
		return this._sideBoundaries.get(aSide).getGridMethod(soluteName);
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
			if ( dim instanceof BoundedDimension )
				for ( Boundary b : ((BoundedDimension) dim).getBoundaries() )
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
	 * \brief Check if a given location, in local dimensions, is inside this
	 * shape.
	 * 
	 * TODO
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
	
	public void applyBoundariesLocal(double[] location)
	{
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			location[i] = dim.applyBoundary(location[i]);
			i++;
		}
		// TODO other boundaries 
	}
	
	protected double[] addSideLength(double[] loc, int dimension)
	{
		double[] out = Vector.copy(loc);
		// TODO out[dimension] += this._lengths[dimension];
		return out;
	}
	
	protected double[] subtractSideLength(double[] loc, int dimension)
	{
		double[] out = Vector.copy(loc);
		// TODO out[dimension] -= this._lengths[dimension];
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param location
	 * @return
	 */
	public LinkedList<double[]> getCyclicPoints(double[] location)
	{
		LinkedList<double[]> out = new LinkedList<double[]>();
		out.add(location);
		LinkedList<double[]> temp = new LinkedList<double[]>();
		double[] newPoint;
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( dim instanceof CyclicDimension )
			{
				for ( double[] loc : out )
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
				out.addAll(temp);
				temp.clear();
			}
			/* Increment the dimension iterator, even if this isn't cyclic. */
			i++;
		}
		return out;
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		/* Check there are no more boundaries required. */
		if ( ! this._requiredBoundarySides.isEmpty() )
		{
			// TODO
			return false;
		}
		/* If there are any other boundaries, check these are ready. */
		Boundary b;
		for ( BoundarySide s : this._sideBoundaries.keySet() )
		{
			b = this._sideBoundaries.get(s);
			if ( ! b.isReadyForLaunch() )
			{
				// TODO
				return false;
			}
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
	
	public static Object getNewInstance(String className)
	{
		return XMLable.getNewInstance(className, "shape.ShapeLibrary$");
	}
}
