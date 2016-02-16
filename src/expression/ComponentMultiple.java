/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public abstract class ComponentMultiple extends Component
{
	protected String _expr;
	
	protected ArrayList<Component> _components; 
	
	public ComponentMultiple(ArrayList<Component> components)
	{
		this._components = components;
	}
	
	public ComponentMultiple(Component a, Component b)
	{
		this._components = new ArrayList<Component>();
		this.appendComponent(a);
		this.appendComponent(b);
	}

	@Override
	public String getName()
	{
		String out = this._components.get(0).getName();
		for ( int i = 1; i < this._components.size(); i++ )
			out += this._expr+this._components.get(i).getName();
		return ( isNegative() ? "-(" : "(") + out + ")";
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		String out = this._components.get(0).reportValue(variables);
		for ( int i = 1; i < this._components.size(); i++ )
			out += this._expr+this._components.get(i).reportValue(variables);
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param component
	 */
	protected void prependComponent(Component component)
	{
		this._components.add(0, component);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param component
	 */
	protected void appendComponent(Component component)
	{
		this._components.add(component);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public ArrayList<Component> getAllComponents()
	{
		return this._components;
	}
}
