package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentDouble;

/**
 * \brief A component of a mathematical expression composed of one component 
 * raised to the power of another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Power extends ComponentDouble
{
	/**
	 * \brief Construct a power component of a mathematical expression
	 * from two sub-components.
	 * 
	 * <p><b>a<sup>b</sup></b></p>
	 * 
	 * @param a The main number.
	 * @param b The exponent.
	 */
	public Power(Component a, Component b)
	{
		super(a, b);
		this._expr = "^";
	}
	
	@Override
	public String getName()
	{
		return this._a.getName() + this._expr + "{" + this._b.getName() + "}";
	}
	
	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		return this._a.reportEvaluation(variables) + this._expr + "{" +
									this._b.reportEvaluation(variables) + "}";
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		double a = this._a.getValue(variables);
		double b = this._b.getValue(variables);
		/* Can't divide by zero! */
		if ( a == 0.0 && b < 0.0 )
			this.infiniteValueWarning();
		return Math.pow(a, b);
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		double a = (double) this._a.evaluate(subject);
		double b = (double) this._b.evaluate(subject);
		/* Can't divide by zero! */
		if ( a == 0.0 && b < 0.0 )
			this.infiniteValueWarning();
		return Math.pow(a, b);
	}
	
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		if ( this._b instanceof Constant )
		{
			if ( this._b.getValue(null) == 1.0 )
				return this._b.differentiate(withRespectTo);
			if ( this._b.getValue(null) == 0.0 )
				return new Constant("0", 0.0);
		}
		Component newIndex = new Subtraction(this._b, Arithmetic.one());
		return Arithmetic.multiply(this._b, new Power(this._a, newIndex));
	}
}