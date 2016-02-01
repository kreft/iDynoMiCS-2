package shape;

import boundary.Boundary;
import generalInterfaces.CanPrelaunchCheck;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class Dimension implements CanPrelaunchCheck
{
	/**
	 * Minimum and maximum values for this dimension. Must be finite and have
	 * {@code this._extreme[0]} < {@code this._extreme[1]}.
	 */
	protected double[] _extreme = new double[]{0.0, Double.MIN_VALUE};
	
	/**
	 * Boundary objects at the minimum (0) and maximum (1). Meaningless in
	 * cyclic dimensions.
	 */
	protected Boundary[] _boundary = new Boundary[2];
	
	/**
	 * Whether boundaries are required (true) or optional (false) at the
	 * minimum (0) and maximum (1) of this dimension. Meaningless in
	 * cyclic dimensions.
	 */
	protected boolean[] _required = new boolean[]{true, true};
	
	/**
	 * If this is a cyclic dimension, different rules apply.
	 */
	protected boolean _isCyclic = false;
	
	/**************************************************************************
	 * BASIC SETTERS AND GETTERS
	 *************************************************************************/
	
	/**
	 * \brief Get the length of this dimension.
	 * 
	 * @return
	 */
	public double getLength()
	{
		return this._extreme[1] - this._extreme[0];
	}
	
	protected void checkExtremes()
	{
		if ( this._extreme[1] <= this._extreme[0] )
		{
			throw new IllegalArgumentException(
					"Dimension length must be >= 0");
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param value
	 * @param index
	 */
	public void setExtreme(double value, int index)
	{
		this._extreme[index] = value;
		this.checkExtremes();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param minValue
	 * @param maxValue
	 */
	public void setExtremes(double minValue, double maxValue)
	{
		this._extreme[0] = minValue;
		this._extreme[1] = maxValue;
		this.checkExtremes();
	}
	
	/**
	 * \brief Set the length of this dimension.
	 * 
	 * @param length Positive {@code double}.
	 */
	public void setLength(double length)
	{
		this._extreme[1] = this._extreme[0] + length;
		this.checkExtremes();
	}
	
	public void setCyclic()
	{
		this._isCyclic = true;
		// TODO safety if boundaries already set
		this._boundary = new Boundary[]{};
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
	
	/**************************************************************************
	 * BOUNDARIES
	 *************************************************************************/
	
	/**
	 * \brief Tell this dimension that the boundary at the minimum extreme may
	 * not be specified. Meaningless in cyclic dimensions.
	 * 
	 * @see #setBoundariesRequired()
	 */
	public void setBoundaryOptional(int index)
	{
		this._required[index] = false;
	}
	
	/**
	 * \brief Tell this dimension that both boundaries may not be specified.
	 * Meaningless in cyclic dimensions.
	 * 
	 * @see #setMinBoundaryRequired()
	 * @see #setMAxBoundaryRequired()
	 */
	public void setBoundariesOptional()
	{
		this.setBoundaryOptional(0);
		this.setBoundaryOptional(1);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aBoundary
	 * @param setMin {@code boolean} specifying whether to set the minimum
	 * boundary (true) or the maximum boundary (false).
	 */
	public void setBoundary(Boundary aBoundary, int index)
	{
		if ( this._isCyclic )
		{
			// TODO
			//throw new Exception();
		}
		else
			this._boundary[index] = aBoundary;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param minBndry
	 * @param maxBndry
	 */
	public void setBoundaries(Boundary minBndry, Boundary maxBndry)
	{
		if ( this._isCyclic )
		{
			// TODO
			//throw new Exception();
		}
		else
		{
			this._boundary[0] = minBndry;
			this._boundary[1] = maxBndry;
		}
	}
	
	/**
	 * \brief Get an array of boundaries. 
	 * 
	 * <p>Note that this will return an empty array if the dimension is cyclic.
	 * Otherwise, this will be a 2-array with the minimum boundary at position
	 * 0 and the maximum boundary at position 1 (optional boundaries may be
	 * {@code null} objects).</p>
	 * 
	 * @return Array of {@code Boundary} objects: empty array if this is
	 * cyclic, a 2-array otherwise.
	 */
	public Boundary[] getBoundaries()
	{
		return this._boundary;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @return
	 */
	public double applyBoundary(double a)
	{
		if ( this._isCyclic )
		{
			// TODO check this modulo behaves with negative a
			return this._extreme[0] +
								( (a - this._extreme[0]) % this.getLength() );
		}
		else
		{
			/*
			 * this._extreme[1] is an exclusive limit, so take a value just
			 * below if necessary.
			 */
			return Math.min( this._extreme[1] - Math.ulp(this._extreme[1]),
												Math.max(this._extreme[0], a));
		}
	}
	
	/**************************************************************************
	 * USEFUL METHODS
	 *************************************************************************/
	
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
		double out = b - a;
		if ( this._isCyclic &&  (Math.abs(out) > 0.5 * this.getLength()) )
			out -= this.getLength() * Math.signum(out);
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @return
	 */
	public boolean isInside(double a)
	{
		/* Always inside a cyclic dimension. */
		return this._isCyclic ||
					(( a >= this._extreme[0] ) && ( a < this._extreme[1] ));
	}
	
	/**************************************************************************
	 * PRE-LAUNCH CHECK
	 *************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		for ( int i = 0; i < 2; i++ )
			if ( this._required[i] && this._boundary[i] == null )
			{
				// TODO check boundary is ready to launch?
				return false;
			}
		return true;
	}
}