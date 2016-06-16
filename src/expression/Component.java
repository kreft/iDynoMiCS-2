/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * \brief Abstract class for any component of a mathematical expression.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class Component
{
	/**
	 * Making a component negative is a convenient shorthand for subtracting it
	 * from zero. 
	 */
	private boolean _isNegative;
	/**
	 * Names of all the variables used in this expression. Storing the names
	 * saves re-collection them every time they are needed.
	 */
	private Collection<String> _varNames = null;
	
	/**
	 * \brief Get the name of this component.
	 * 
	 * @return {@code String} name of this component.
	 */
	public abstract String getName();
	
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
	public abstract double getValue(Map<String, Double> variables);
	
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
	
	/**
	 * \brief Get a list of names for all the variables in this mathematical
	 * expression.
	 * 
	 * @return List of names of variables. Order is irrelevant.
	 */
	public Collection<String> getAllVariablesNames()
	{
		if ( this._varNames == null )
		{
			this._varNames = new ArrayList<String>();
			this.appendVariablesNames(this._varNames);
		}
		return this._varNames;
	}
	
	/**
	 * \brief Helper method for {@link #getAllVariableNames()}. 
	 * 
	 * <p>Gets sub-components to add their variable names to the list given to
	 * them.</p>
	 * 
	 * @param names List of names of variables so far found in this
	 * {@code Component}.
	 */
	protected abstract void appendVariablesNames(Collection<String> names);
}