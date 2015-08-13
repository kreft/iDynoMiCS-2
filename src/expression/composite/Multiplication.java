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
public class Multiplication extends CompositeComponent
{
	/**
	 * <b>a</b> * <b>b</b>
	 */
	public Multiplication(Component a, Component b)
	{
		super(a, b);
		this._expr = "*";
	}

	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._a.getValue(variables) * this._b.getValue(variables);
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