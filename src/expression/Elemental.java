package expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public abstract class Elemental {
	
	private Comparable<?> element;

	private Type _type;
	/**
	 * Names of all the variables used in this expression. Storing the names
	 * saves re-collection them every time they are needed.
	 */
	private Collection<String> _varNames = null;
		
	public enum Type {
		numeric,
		bool,
		string,
		omnifarious;
	}
	
	/**
	 * \brief Get the name of this component.
	 * 
	 * @return {@code String} name of this component.
	 */
	public abstract String getName();
	
	public abstract Elemental getValueEle(Map<String, Object> variables);
	
	public Elemental(Type type)
	{
		this._type = type;
		
		Boolean a = true;
	}
	
	public Type type()
	{
		return this._type;
	}
	
	public void set(Type type)
	{
		this._type = type;
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


	public Comparable<?> getObj() {
		return element;
	}


	public void setObj(Comparable<?> obj) {
		this.element = obj;
	}
}