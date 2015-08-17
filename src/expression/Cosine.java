/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public class Cosine extends ComponentSingle
{

	/**\brief TODO
	 * 
	 */
	public Cosine(Component a)
	{
		super(a);
		
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return Math.cos(this._a.getValue(variables));
	}
	
	@Override
	protected Component getDifferential(String withRespectTo)
	{
		Sine out = new Sine(this._a);
		out.changeSign();
		return out;
	}

}
