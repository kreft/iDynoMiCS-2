/**
 * 
 */
package expression.arithmetic;

import java.util.ArrayList;
import java.util.Map;

import expression.Component;
import expression.ComponentMultiple;

/**
 * \brief A component of a mathematical expression composed of the
 * multiplication of two or more sub-components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Multiplication extends ComponentMultiple
{
	/**
	 * \brief Construct a multiplication component of a mathematical
	 * expression from a list of sub-components.
	 * 
	 * @param a List of sub-components to multiply.
	 */
	public Multiplication(ArrayList<Component> a)
	{
		super(a);
		this._expr = "*";
	}
	
	/**
	 * \brief Construct a multiplication component of a mathematical expression
	 * from two sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public Multiplication(Component a, Component b)
	{
		super(a, b);
		this._expr = "*";
	}
	
	@Override
	public double calculateValue(Map<String, Double> variables)
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