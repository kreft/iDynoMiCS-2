/**
 * 
 */
package expression;

import java.util.Map;

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
	public double getValue(Map<String, Double> variables)
	{
		return Math.cos(this._a.getValue(variables));
	}
	
	@Override
	protected Component getDifferential(String withRespectTo)
	{
		Component dV = this._a.differentiate(withRespectTo);
		if ( Expression.isConstantWithValue(dV, 0.0) )
			return dV;
		Sine dU = new Sine(this._a);
		dU.changeSign();
		return Expression.multiply(dU, dV);
	}

}
