package expression.composite;

import java.util.HashMap;

import expression.Component;
import expression.CompositeComponent;
import expression.simple.Constant;

public class Power extends CompositeComponent
{
	/**
	 * <b>a<sup>b</sup></b>
	 */
	public Power(Component a, Component b)
	{
		super(a, b);
		this._expr = "^";
	}
	
	@Override
	public String getName()
	{
		return this._a.getName() + this._expr + "{" + this._b.getName() + "}";
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		return this._a.reportValue(variables) + this._expr + "{" +
										this._b.reportValue(variables) + "}";
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		double a = this._a.getValue(variables);
		double b = this._b.getValue(variables);
		if ( a == 0.0 && b < 0.0 )
			this.infiniteValueWarning(variables);
		return Math.pow(a, b);
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		if ( this._b instanceof Constant )
		{
			if ( this._b.getValue(null) == 1.0 )
				return this._b.differentiate(withRespectTo);
			if ( this._b.getValue(null) == 0.0 )
				return new Constant("0", 0.0);
		}
		Component newIndex = new Subtraction(this._b, Constant.one());
		return new Multiplication(this._b, new Power(this._a, newIndex));
	}
}