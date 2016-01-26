/**
 * 
 */
package shape;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import agent.AgentBoundary.AgentMethod;
import boundary.Boundary;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.GridBoundary.GridMethod;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Shape implements CanPrelaunchCheck, XMLable
{
	protected int _nDim;
	/**
	 * 3-dimensional vector describing the size of this shape.
	 * 
	 * <p>Even 0-, 1- and 2-dimensional shapes may need to have a thickness on
	 * their "missing" dimension(s).</p>
	 */
	protected double[] _lengths;
	
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
		this._lengths = new double[3];
		this._requiredBoundarySides = new LinkedList<BoundarySide>();
		this._sideBoundaries = new HashMap<BoundarySide, Boundary>();
		this._otherBoundaries = new LinkedList<Boundary>();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Gets the side lengths of all dimensions.
	 * 
	 * @param lengths {@code double} array of all side lengths.
	 * @see #getEdgeLengths()
	 */
	public double[] getSideLengths()
	{
		return Vector.copy(this._lengths);
	}
	
	/**
	 * \brief Gets the side lengths of only the significant dimensions.
	 * 
	 * @param lengths {@code double} array of significant side lengths.
	 * @see #getSideLengths()
	 */
	public double[] getEdgeLengths()
	{
		return Vector.subset(this._lengths, this._nDim);
	}
	
	/**
	 * \brief Set the side lengths of this shape.
	 * 
	 * <p>NOTE: If lengths has more than 3 elements, the extra elements will be
	 * ignored. If lengths has fewer than 3 elements, the extra sides will be
	 * given zero length.</p>
	 * 
	 * @param lengths {@code double} array of side lengths.
	 */
	public void setSideLengths(double[] lengths)
	{
		int maxDim = Math.min(3, lengths.length);
		for ( int i = 0; i < maxDim; i++ )
			this._lengths[i] = lengths[i];
		for ( int i = maxDim; i < 3; i++ )
			this._lengths[i] = 0.0;
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
		return this._nDim;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract GridGetter gridGetter();
	
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
	public Collection<Boundary> getSideBoundaries()
	{
		return this._sideBoundaries.values();
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
	 * 
	 * @param aSide
	 * @param loc
	 * @return
	 */
	public abstract double[] getCyclicPoint(BoundarySide aSide, double[] loc);
	
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
		Boundary b;
		double[] point;
		for ( BoundarySide bS : this._sideBoundaries.keySet() )
		{
			b = this._sideBoundaries.get(bS);
			if ( b.getAgentMethod(null).isCyclic() )
			{
				LinkedList<double[]> temp = new LinkedList<double[]>();
				for ( double[] loc : out )
				{
					point = this.getCyclicPoint(bS, loc);
					if ( point != null )
						temp.add(point);
				}
				out.addAll(temp);
			}
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
