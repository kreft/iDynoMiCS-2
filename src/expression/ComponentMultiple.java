/**
 * 
 */
package expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import expression.Elemental.Type;

/**
 * \brief A component of a mathematical expression composed of two or more
 * sub-components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ComponentMultiple extends Component
{
	/**
	 * {@code String} description of the expression 
	 */
	protected String _expr;
	/**
	 * List of all sub-components.
	 */
	protected ArrayList<Component> _components; 
	
	/**
	 * \brief Construct a component of a mathematical expression from a list of
	 * sub-components.
	 * 
	 * @param components List of sub-components.
	 */
	public ComponentMultiple(ArrayList<Component> components)
	{
		super(Type.numeric);
		this._components = components;
	}
	
	/**
	 * \brief Construct a component of a mathematical expression from two
	 * sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public ComponentMultiple(Component a, Component b)
	{
		super(Type.numeric);
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
	public String reportEvaluation(Map<String, Double> variables)
	{
		String out = this._components.get(0).reportEvaluation(variables);
		for ( int i = 1; i < this._components.size(); i++ )
			out += this._expr+this._components.get(i).reportEvaluation(variables);
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	/**
	 * \brief Add a sub-component to the start of this
	 * {@code ComponentMultiple}'s list.
	 * 
	 * <p>Note that the list order matters only for readability, and should 
	 * makes no difference to the evaluation.</p>
	 * 
	 * @param component Sub-component to add.
	 */
	public void prependComponent(Component component)
	{
		this._components.add(0, component);
	}
	
	/**
	 * \brief Add a sub-component to the end of this
	 * {@code ComponentMultiple}'s list.
	 * 
	 * @param component Sub-component to add.
	 */
	public void appendComponent(Component component)
	{
		this._components.add(component);
	}
	
	/**
	 * \brief Get the list of all sub-components that comprise this
	 * {@code ComponentMultiple}.
	 * 
	 * @return List of {@code Component}s.
	 */
	public ArrayList<Component> getAllComponents()
	{
		return this._components;
	}
	
	@Override
	public void appendVariablesNames(Collection<String> names)
	{
		for ( Component c : this._components )
			if( c != null )
				c.appendVariablesNames(names);
	}
}
