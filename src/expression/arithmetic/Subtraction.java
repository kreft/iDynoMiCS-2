package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentDouble;

/**
 * \brief A component of a mathematical expression composed of the subtraction
 * of one component from another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Subtraction extends ComponentDouble
{
	/**
	 * \brief Construct a subtraction component of a mathematical expression
	 * from two sub-components.
	 * 
	 * <p><b>a</b> - <b>b</b></p>
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public Subtraction(Component a, Component b)
	{
		super(a, b);
		this._expr = "-";
	}

	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		return this._a.getValue(variables) - this._b.getValue(variables);
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		return (double) this._a.evaluate(subject) - (double) this._b.evaluate(subject);
	}

	@Override
	public Component differentiate(String withRespectTo)
	{
		Component da = this._a.differentiate(withRespectTo);
		Component db = this._b.differentiate(withRespectTo);
		if ( this._a instanceof Constant )
			return db;
		if ( this._b instanceof Constant )
			return da;
		return new Subtraction(da, db);
	}
}