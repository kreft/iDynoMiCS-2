/**
 * 
 */
package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentSingle;

/**
 * \brief Component of a mathematical expression that is the sine of another
 * component.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Sine extends ComponentSingle
{
	/**
	 * \brief Construct a cosine component of a mathematical expression from
	 * a sub-component.
	 * 
	 * @param a {@code Component} whose cosine will be evaluated.
	 */
	public Sine(Component a)
	{
		super(a);
		this._expr = "sin";
		/* sin(-x) = -sin(x) */
		if ( this._a.isNegative() )
		{
			this._a.changeSign();
			this.changeSign();
		}
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		return Math.sin(this._a.getValue(variables));
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		return Math.sin((double) this._a.evaluate(subject));
	}
	
	@Override
	protected Component getDifferential(String withRespectTo)
	{
		Component dV = this._a.differentiate(withRespectTo);
		if ( Arithmetic.isConstantWithValue(dV, 0.0) )
			return dV;
		Cosine dU = new Cosine(this._a);
		return Arithmetic.multiply(dU, dV);
	}
}
