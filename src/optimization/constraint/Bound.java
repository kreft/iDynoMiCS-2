package optimization.constraint;

import java.util.function.Predicate;

public class Bound implements Predicate<Double> {
	
	protected Double _bound;
	protected boolean _sign;

	public Bound(Double bound, boolean upper)
	{
		this._bound = bound;
		this._sign = upper;
	}

	@Override
	public boolean test(Double t) 
	{
		if (this._sign)
			return ( t <= this._bound );
		else
			return ( t >= this._bound );
	}
	
	public double bound()
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
		return (this._sign ? "<=" : " => ") + this._bound;
	}
}
