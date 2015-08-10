package expression;

import java.util.HashMap;

import utility.LogFile;

public abstract class CompositeComponent extends Component
{
	protected String _expr;
	
	/**
	 * Used by all composite components.
	 */
	protected Component _a, _b;
	
	public CompositeComponent(Component a, Component b)
	{
		this._a = a;
		this._b = b;
	}
	
	public String getName()
	{
		return this._a.getName() + this._expr + this._b.getName();
	}
	
	public String reportValue(HashMap<String, Double> variables)
	{
		return this._a.reportValue(variables) + this._expr +
											this._b.reportValue(variables);
	}
	
	protected void infiniteValueWarning(HashMap<String, Double> variables)
	{
		LogFile.writeLog("WARNING! Infinite value: " + this.getName() + 
										" = " + this.reportValue(variables));
	}
	
}