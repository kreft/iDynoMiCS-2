package expression;

import java.util.Map;

/**
 * \brief A component of a mathematical expression composed of the division
 * of one component by another.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Division extends ComponentDouble
{
	/**
	 * \brief Construct a division component of a mathematical expression
	 * from two sub-components.
	 * 
	 * <p><b>a</b> / <b>b</b></p>
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public Division(Component a, Component b)
	{
		super(a, b);
		this._expr = "/";
	}
	
	@Override
	public double getValue(Map<String, Double> variables)
	{
		double b = this._b.getValue(variables);
		if ( b == 0.0 )
			this.infiniteValueWarning(variables);
		return this._a.getValue(variables) / b;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		Multiplication aDb = Expression.multiply(this._a, 
									this._b.differentiate(withRespectTo));
		Multiplication bDa = Expression.multiply(this._b,
									this._a.differentiate(withRespectTo));
		if ( this._a instanceof Constant )
			return aDb;
		if ( this._b instanceof Constant )
			return bDa;
		return Expression.add(aDb, bDa);
	}
	
	/**
	 * \brief Get the numerator of this expression, i.e. what is on top of the
	 * fraction.
	 * 
	 * @return Numerator component.
	 */
	public Component getNumerator()
	{
		return this._a;
	}
	
	/**
	 * \brief Get the denominator of this expression, i.e. what is on the
	 * bottom of the fraction.
	 * 
	 * @return Denominator component.
	 */
	public Component getDenominator()
	{
		return this._b;
	}
}