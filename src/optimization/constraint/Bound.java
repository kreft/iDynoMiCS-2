package optimization.constraint;

import linearAlgebra.Vector;

public class Bound implements Constraint {
	
	protected double[] _bound;
	protected boolean _sign;

	public Bound(double[] bound, boolean isUpperBound)
	{
		this._bound = bound;
		this._sign = isUpperBound;
	}

	@Override
	public boolean test( double[] values ) 
	{
		for(int i = 0; i < values.length; i++)
		{
			if (this._sign)
			{
				if ( values[i] > this._bound[i] )
					return false;
			}
			else
			{
				if ( values[i] < this._bound[i] )
					return false;
			}
		}
		return true;
	}	
	
	public boolean test( double value, int position ) 
	{
		if (this._sign)
		{
			if ( value > this._bound[position] )
				return false;
		}
		else 
		{
			if ( value < this._bound[position] )
				return false;
		}
		return true;
	}	
	
	public double[] bound()
	{
		return this._bound;
	}
	
	public boolean isUpperBound()
	{
		return this._sign;
	}
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return (this._sign ? "<=" : " => ") + Vector.toString( this._bound );
	}
}
