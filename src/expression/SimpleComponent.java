/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public abstract class SimpleComponent extends Component
{
	protected String _name;
	
	/**\brief TODO
	 * 
	 */
	public SimpleComponent(String name)
	{
		this._name = name;
	}
	
	public String getName()
	{
		return this._name;
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		return Double.toString(this.getValue(variables));
	}
}
