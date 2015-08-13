package expression.composite;

import java.util.HashMap;

import expression.Component;
import expression.CompositeComponent;
import expression.simple.Constant;

public class Subtraction extends CompositeComponent
{
	/**
	 * <b>a</b> - <b>b</b>
	 */
	public Subtraction(Component a, Component b)
	{
		super(a, b);
		this._expr = "-";
	}

	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._a.getValue(variables) - this._b.getValue(variables);
	}

	@Override
	public Component differentiate(String withRespectTo)
	{
		Component da = this._a.differentiate(withRespectTo);
		Component db = this._b.differentiate(withRespectTo);
		if ( this._a instanceof Constant )
			return db;
		if ( this._b instanceof Constant )
			return da;
		return new Subtraction(da, db);
	}
}