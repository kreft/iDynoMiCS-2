/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public class LogNatural extends ComponentSingle
{

	/**\brief TODO
	 * 
	 * @param a
	 */
	public LogNatural(Component a)
	{
		super(a);
		this._expr = "ln";
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return Math.log(this._a.getValue(variables));
	}
	
	@Override
	protected Component getDifferential(String withRespectTo)
	{
		return new Division(this._a.differentiate(withRespectTo), this._a);
	}

}
