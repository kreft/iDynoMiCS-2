package expression;

import java.util.Collection;
import java.util.Map;

import dataIO.Log;

public abstract class ComponentBoolean extends Component {

	protected String _expr;

	protected ComponentNumerical _a, _b;

	public ComponentBoolean(ComponentNumerical a, ComponentNumerical b)
	{
		this._a = a;
		this._b = b;
	}
	
	@Override
	public String getName()
	{
		return ( isNegative() ? "-(" : "(") + this._a.getName() + this._expr + 
				this._b.getName() + ")";
	}
	
	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		String out = this._a.reportEvaluation(variables) + this._expr +
											this._b.reportEvaluation(variables);
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	@Override
	public void appendVariablesNames(Collection<String> names)
	{
		this._a.appendVariablesNames(names);
		this._b.appendVariablesNames(names);
	}
	
	/**
	 * \brief Helper method for sub-classes that may encounter infinite values.
	 * 
	 * @param variables Dictionary of variable names with associated values
	 * that triggered the infinite value.
	 */
	protected void infiniteValueWarning(Map<String, Double> variables)
	{
		Log.out(Log.Tier.CRITICAL,"WARNING! Infinite value: " + this.getName() + 
									" = " + this.reportEvaluation(variables));
	}
}
