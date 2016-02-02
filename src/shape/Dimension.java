package shape;

import boundary.Boundary;
import generalInterfaces.CanPrelaunchCheck;

/**
 * 
 * TODO start point? I.e. not necessarily starting at zero
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class Dimension implements CanPrelaunchCheck
{
	/**
	 * Total length of this dimension. Must be >= 0.
	 */
	protected double _length;
	
	/**
	 * If this is a cyclic dimension, different rules apply.
	 */
	protected boolean _isCyclic = false;
	
	/**
	 * Boundary objects at the minimum (0) and maximum (1). Meaningless in
	 * cyclic dimensions.
	 */
	protected Boundary[] _boundaries = new Boundary[2];
	
	/**
	 * Whether boundaries are required (true) or optional (false) at the
	 * minimum (0) and maximum (1) of this dimension. Meaningless in
	 * cyclic dimensions.
	 */
	protected boolean[] _required = new boolean[]{true, true};
	
	/**
	 * \brief Get the length of this dimension.
	 * 
	 * @return
	 */
	public double getLength()
	{
		return this._length;
	}
	
	/**
	 * \brief Set the length of this dimension.
	 * 
	 * @param length Non-negative {@code double}.
	 */
	public void setLength(double length)
	{
		if ( length < 0.0 )
		{
			throw new IllegalArgumentException(
					"Dimension length must be >= 0");
		}
		this._length = length;
	}
	
	public void setCyclic()
	{
		this._isCyclic = true;
		// TODO safety if boundaries already set
		this._boundaries = new Boundary[]{};
	}
	
	/**
	 * \brief Whether this dimension is cyclic or not.
	 * 
	 * @return {@code boolean} specifying whether this dimension is cyclic
	 * (true) or bounded (false).
	 */
	public boolean isCyclic()
	{
		return this._isCyclic;
	}
	
	/*
	 * Boundaries
	 */
	
	protected int m(boolean setMin)
	{
		return setMin ? 0 : 1;
	}
	
	/**
	 * \brief Tell this dimension that the boundary at the given extreme
	 * may not be specified. Meaningless in cyclic dimensions.
	 * 
	 * @param setMin {@code boolean} specifying whether to set the minimum
	 * (true) or the maximum (false).
	 * @see #setBoundariesRequired()
	 */
	public void setBoundaryOptional(boolean setMin)
	{
		this._required[m(setMin)] = false;
	}
	
	/**
	 * \brief Tell this dimension that both boundaries may not be specified.
	 * Meaningless in cyclic dimensions.
	 * 
	 * @see #setBoundaryRequired(boolean)
	 */
	public void setBoundariesOptional()
	{
		this._required[0] = false;
		this._required[1] = false;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aBoundary
	 * @param setMin {@code boolean} specifying whether to set the minimum
	 * boundary (true) or the maximum boundary (false).
	 */
	public void setBoundary(Boundary aBoundary, boolean setMin)
	{
		if ( this._isCyclic )
		{
			// TODO
			//throw new Exception();
		}
		else
			this._boundaries[m(setMin)] = aBoundary;
	}
	
	public Boundary[] getBoundaries()
	{
		return this._boundaries;
	}
	
	/*
	 * Useful methods
	 */
	
	/**
	 * \brief Get the shortest distance between two positions along this
	 * dimension.
	 * 
	 * <p>Note that this may be negative if <b>b</b> > <b>a</b>.</p> 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double getShortest(double a, double b)
	{
		double out = a - b;
		//System.out.println("a - b: "+out);
		if ( this._isCyclic &&  (Math.abs(out) > 0.5 * this._length) )
		{
			out -= this._length * Math.signum(out);
			//System.out.println("cyclic a - b: "+out);
		}
		return out;
	}
	
	public double applyBoundary(double a)
	{
		if ( this._isCyclic )
		{
			// TODO check this modulo behaves with negative numbers
			return a % this._length;
		}
		else
		{
			// TODO use length minus some tidy amount?
			return Math.max(0.0, Math.min(this._length, a));
		}
	}
	
	public boolean isInside(double a)
	{
		/* Always inside a cyclic dimension. */
		return this._isCyclic || (( a >= 0.0 ) && ( a < this._length ));
	}
	
	/**************************************************************************
	 * PRE-LAUNCH CHECK
	 *************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		for ( int i = 0; i < 2; i++ )
			if ( this._required[i] && this._boundaries[i] == null )
			{
				// TODO check boundary is ready to launch?
				return false;
			}
		return true;
	}
}