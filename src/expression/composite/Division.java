package expression.composite;

import java.util.HashMap;

import expression.Component;
import expression.CompositeComponent;
import expression.simple.Constant;


public class Division extends CompositeComponent
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
		Multiplication aDb = new Multiplication(this._a, 
									this._b.differentiate(withRespectTo));
		Multiplication bDa = new Multiplication(this._b,
									this._a.differentiate(withRespectTo));
		if ( this._a instanceof Constant )
			return aDb;
		if ( this._b instanceof Constant )
			return bDa;
		return new Addition(aDb, bDa);
	}
}