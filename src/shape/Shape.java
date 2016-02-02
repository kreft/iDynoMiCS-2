/**
 * 
 */
package shape;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import boundary.Boundary;
import boundary.BoundaryConnected;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.ShapeConventions.BoundarySide;
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
		this._dimensions = new LinkedHashMap<DimName, Dimension>();
		this._otherBoundaries = new LinkedList<Boundary>();
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
			return null;
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
	
	public void setBoundary(DimName dimension, Boundary bndry, int index)
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
		this.setBoundary(DimName.valueOf(dimension), bndry, index);
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
	
	protected abstract double[] getGlobalLocation(double[] local);
	
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
		DimName dimN = aSide.dim;
		if ( dimN == null )
		{
			// TODO Rob [14Jan2015]: separate lists for internal & connection
			// boundaries? 
			this._otherBoundaries.add(aBoundary);
		}
		else
		{
			// TODO Rob [14Jan2015]: throw an error/warning if a side boundary
			// is being overwritten?
			// TODO Rob [28Jan2016]: throw an error if this dimension is not in
			// our list?
			Dimension dim = this._dimensions.get(dimN);
			int index = (aSide == dimN.minBndry) ? 0 : 1;
			dim.setBoundary(aBoundary, index);
		}
	}
	
	public Boundary getBoundary(BoundarySide aSide)
	{
		DimName dimN = aSide.dim;
		if ( dimN == null )
			return null;
		Dimension dim = this._dimensions.get(dimN);
		return dim.getBoundaries()[aSide == dimN.minBndry ? 0 : 1];
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
	 * @param a
	 * @param b
	 * @return
	 */
	public double[] getMinDifference(double[] a, double[] b)
	{
		// TODO safety with vector length & number of dimensions
		// TODO check this is the right approach in polar geometries
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
	
	public static Object getNewInstance(String className)
	{
		return XMLable.getNewInstance(className, "shape.ShapeLibrary$");
	}
}
