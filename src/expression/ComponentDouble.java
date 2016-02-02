package expression;

import java.util.HashMap;

import dataIO.LogFile;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public abstract class ComponentDouble extends Component
{
	protected String _expr;
	
	protected Component _a, _b;
	
	public ComponentDouble(Component a, Component b)
	{
		this._a = a;
		this._b = b;
	}
	
	@Override
	public String getName()
	{
		String out = this._a.getName() + this._expr + this._b.getName();
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		String out = this._a.reportValue(variables) + this._expr +
											this._b.reportValue(variables);
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	protected void infiniteValueWarning(HashMap<String, Double> variables)
	{
		LogFile.writeLog("WARNING! Infinite value: " + this.getName() + 
										" = " + this.reportValue(variables));
	}
}