/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public class Addition extends ComponentMultiple
{
	public Addition(ArrayList<Component> a)
	{
		super(a);
		this._expr = "+";
	}
	
	public Addition(Component a, Component b)
	{
		super(a, b);
		this._expr = "+";
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		double out = 0.0;
		for ( Component c : this._components )
			out += c.getValue(variables);
		return out;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		ArrayList<Component> out = new ArrayList<Component>();
		for ( Component c : this._components )
			if ( ! (c instanceof Constant) )
				out.add(c.differentiate(withRespectTo));
		return new Multiplication(out);
	}
}