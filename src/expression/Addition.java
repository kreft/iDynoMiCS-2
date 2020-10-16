/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.Map;

/**
 * \brief A component of a mathematical expression composed of the addition of
 * two or more sub-components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Addition extends ComponentMultiple
{
	/**
	 * \brief Construct an addition component of a mathematical expression from
	 * a list of sub-components.
	 * 
	 * @param a List of sub-components to add.
	 */
	public Addition(ArrayList<ComponentNumerical> a)
	{
		super(a);
		this._expr = "+";
	}
	
	/**
	 * \brief Construct an addition component of a mathematical expression from
	 * two sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public Addition(ComponentNumerical a, ComponentNumerical b)
	{
		super(a, b);
		this._expr = "+";
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		double out = 0.0;
		for ( ComponentNumerical c : this._components )
			out += c.getValue(variables);
		return out;
	}

	public ComponentNumerical differentiate(String withRespectTo)
	{
		ArrayList<ComponentNumerical> out = new ArrayList<ComponentNumerical>();
		for ( ComponentNumerical c : this._components )
			if ( ! (c instanceof Constant) )
				out.add(c.differentiate(withRespectTo));
		return new Multiplication(out);
	}
}