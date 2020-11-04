/**
 * 
 */
package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentDouble;

/**
 * \brief A component of a mathematical expression composed of the addition of
 * two or more sub-components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Addition extends ComponentDouble
{
	/**
	 * \brief Construct an addition component of a mathematical expression from
	 * two sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public Addition(Component a, Component b)
	{
		super(a, b);
		this._expr = "+";
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		return _a.getValue(variables) + _b.getValue(variables);
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		return new Multiplication((_a instanceof Constant ? Arithmetic.one() : _a.differentiate(withRespectTo)),
				(_b instanceof Constant ? Arithmetic.one() : _b.differentiate(withRespectTo)));
	}

	@Override
	public Object evaluate(AspectInterface subject) 
	{
		return (double) _a.evaluate(subject) + (double) _b.evaluate(subject);
	}
}