/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public abstract class Component
{
	private boolean _isNegative;
	
	public abstract String getName();
	
	public abstract String reportValue(HashMap<String, Double> variables);
	
	public abstract double getValue(HashMap<String, Double> variables);
	
	public abstract Component differentiate(String withRespectTo);
	
	public boolean isNegative()
	{
		return this._isNegative;
	}
	
	public void changeSign()
	{
		this._isNegative = ! this._isNegative;
	}
}