/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Constant extends ComponentSimple
{
	protected double _value;
	
	public Constant(String name, double value)
	{
		super(name);
		this._value = value;
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		return Double.toString(this._value);
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._value;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		return Expression.zero();
	}
}