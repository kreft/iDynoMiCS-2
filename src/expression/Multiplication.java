/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.Map;

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
	public Multiplication(ArrayList<ComponentNumerical> a)
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
	public Multiplication(ComponentNumerical a, ComponentNumerical b)
	{
		super(a, b);
		this._expr = "*";
	}
	
	@Override
	public double calculateValue(Map<String, Double> variables)
	{
		double out = 1.0;
		for ( ComponentNumerical c : this._components )
			out *= c.getValue(variables);
		return out;
	}
	
	@Override
	public ComponentNumerical differentiate(String withRespectTo)
	{
		ArrayList<ComponentNumerical> out = new ArrayList<ComponentNumerical>();
		ArrayList<ComponentNumerical> temp;
		ComponentNumerical c;
		for ( int i = 0; i < out.size(); i++ )
		{
			c = this._components.get(i);
			if ( c instanceof Constant )
				continue;
			temp = new ArrayList<ComponentNumerical>();
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