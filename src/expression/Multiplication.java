/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Multiplication extends ComponentMultiple
{
	
	public Multiplication(ArrayList<Component> a)
	{
		super(a);
		this._expr = "*";
	}
	
	public Multiplication(Component a, Component b)
	{
		super(a, b);
		this._expr = "*";
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		double out = 1.0;
		for ( Component c : this._components )
			out *= c.getValue(variables);
		return out;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		ArrayList<Component> out = new ArrayList<Component>();
		ArrayList<Component> temp;
		Component c;
		for ( int i = 0; i < out.size(); i++ )
		{
			c = this._components.get(i);
			if ( c instanceof Constant )
				continue;
			temp = new ArrayList<Component>();
			temp.addAll(this._components);
			temp.set(i, c.differentiate(withRespectTo));
			out.add(new Multiplication(temp));
		}
		if ( out.size() == 1 )
			return out.get(0);
		else
			return new Addition(out);
	}
}