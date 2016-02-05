package expression;

import java.util.HashMap;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Division extends ComponentDouble
{
	/**
	 * <b>a</b> / <b>b</b>
	 */
	public Division(Component a, Component b)
	{
		super(a, b);
		this._expr = "/";
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		double b = this._b.getValue(variables);
		if ( b == 0.0 )
			this.infiniteValueWarning(variables);
		return this._a.getValue(variables) / b;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		Multiplication aDb = Expression.multiply(this._a, 
									this._b.differentiate(withRespectTo));
		Multiplication bDa = Expression.multiply(this._b,
									this._a.differentiate(withRespectTo));
		if ( this._a instanceof Constant )
			return aDb;
		if ( this._b instanceof Constant )
			return bDa;
		return Expression.add(aDb, bDa);
	}
	
	public Component getNumerator()
	{
		return this._a;
	}
	
	public Component getDenominator()
	{
		return this._b;
	}
}