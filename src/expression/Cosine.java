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
		// TODO Auto-generated method stub
		return new Multiplication(Constant.minus(), new Sine(this._a));
	}

}
