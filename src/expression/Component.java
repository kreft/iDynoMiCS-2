/**
 * 
 */
package expression;
import java.util.HashMap;
import java.util.Map;

import aspect.AspectInterface;

/**
 * \brief Abstract class for any component of a mathematical expression.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class Component extends Elemental
{
	
	public Component(Type type) {
		super(type);
	}

	/**
	 * Making a component negative is a convenient shorthand for subtracting it
	 * from zero. 
	 */
	private boolean _isNegative;

	/**
	 * \brief Report the evaluation of this mathematical expression as a
	 * {@code String}. 
	 * 
	 * <p>The purpose of this method is to help the user understand what is
	 * happening inside this mathematical expression.</p>
	 * 
	 * <p>For example, if this component describes the expression 
	 * <i>"(a * b) + c"</i> and <b>variables</b> dictates that <i>a = 1<i>, 
	 * <i>b = 2<i>, and <i>c = 3<i>, then this method should return the string
	 * <i>"(1 * 2) + 3"</i>.
	 * 
	 * @param variables Dictionary of variable names with associated values.
	 * @return {@code String} description of the evaluation of this
	 * mathematical expression.
	 */
	public abstract String reportEvaluation(Map<String, Double> variables);

	/**
	 * \brief Evaluate this mathematical expression.
	 * 
	 * @param variables Dictionary of variable names with associated values.
	 * @return Real number value of the evaluation of this mathematical
	 * expression.
	 */
	public double getValue(Map<String, Double> variables)
	{
		double out = this.calculateValue(variables);
		return ( this._isNegative ) ? -out : out;
	}
	
	/**
	 * \brief Internal helper method for {@link #getValue(Map)}: that deals
	 * with {@link #_isNegative}, so this doesn't have to.
	 * 
	 * @param variables Dictionary of variable names with associated values.
	 * @return Real number value of the evaluation of this mathematical
	 * expression, when assumed positive.
	 */
	protected abstract double calculateValue(Map<String, Double> variables);
	
	/**
	 * \brief Differentiate this mathematical expression with respect to the
	 * given component, be it a variable or a constant.
	 * 
	 * @param withRespectTo {@code String} name of the component that this
	 * mathematical expression should be differentiated with respect to.
	 * @return New {@code Component} describing the differential of this
	 * mathematical expression with respect to the given component name.
	 */
	public abstract Component differentiate(String withRespectTo);
	
	/**
	 * \brief Check if this should be evaluated with the opposite sign to that
	 * expected.
	 * 
	 * @return {@code boolean}: true if the sign should be switched, false if
	 * the sign is correct as it is.
	 */
	public boolean isNegative()
	{
		return this._isNegative;
	}
	
	/**
	 * \brief Change the sign of this mathematical expression.
	 */
	public void changeSign()
	{
		this._isNegative = ! this._isNegative;
	}
}