/**
 * 
 */
package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentSingle;

/**
 * \brief Component of a mathematical expression that is the cosine of another
 * component.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Cosine extends ComponentSingle
{
	/**
	 * \brief Construct a cosine component of a mathematical expression from
	 * a sub-component.
	 * 
	 * @param a {@code Component} whose cosine will be evaluated.
	 */
	public Cosine(Component a)
	{
		super(a);
		this._expr = "cos";
		/* cos(-x) = cos(x) */
		if ( this._a.isNegative() )
			this._a.changeSign();
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		return Math.cos(this._a.getValue(variables));
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		return Math.cos((double) _a.evaluate(subject));
	}
	
	@Override
	protected Component getDifferential(String withRespectTo)
	{
		Component dV = this._a.differentiate(withRespectTo);
		if ( Arithmetic.isConstantWithValue(dV, 0.0) )
			return dV;
		Sine dU = new Sine(this._a);
		dU.changeSign();
		return Arithmetic.multiply(dU, dV);
	}

}
