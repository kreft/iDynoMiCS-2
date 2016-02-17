/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public List<String> getAllVariablesNames()
	{
		List<String> names = new ArrayList<String>();
		this.appendVariablesNames(names);
		return names;
	}
	
	protected abstract void appendVariablesNames(List<String> names);
}