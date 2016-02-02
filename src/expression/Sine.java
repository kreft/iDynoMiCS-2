/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public class Sine extends ComponentSingle
{
	public Sine(Component a)
	{
		super(a);
		this._expr = "sin";
	}
	
	@Override
	protected Component getDifferential(String withRespectTo)
	{
		return Expression.multiply(new Cosine(this._a),
										this._a.differentiate(withRespectTo));
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return Math.sin(this._a.getValue(variables));
	}
}
