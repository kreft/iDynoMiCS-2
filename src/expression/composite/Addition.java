/**
 * 
 */
package expression.composite;

import java.util.HashMap;

import expression.Component;
import expression.CompositeComponent;
import expression.simple.Constant;

/**
 * @author cleggrj
 *
 */
public class Addition extends CompositeComponent
{
	/**
	 * <b>a</b> + <b>b</b>
	 */
	public Addition(Component a, Component b)
	{
		super(a, b);
		this._expr = "+";
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._a.getValue(variables) + this._b.getValue(variables);
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
		return new Addition(da, db);
	}
}