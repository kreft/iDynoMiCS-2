/**
 * 
 */
package expression;

import java.util.Collection;
import java.util.Map;

/**
 * \brief A component of a mathematical expression whose value varies, and so
 * must be given whenever the expression is to be evaluated.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Variable extends ComponentSimple
{
	/**
	 * \brief Construct a variable from its name.
	 * 
	 * @param name {@code String} name for this variable.
	 */
	public Variable(String name)
	{
		super(name);
	}
	
	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		if ( variables.containsKey(this._name) )
			return Double.toString(this.getValue(variables));
		else
		{
			/* Returning a bunch of question marks should help debugging. */
			return "???";
		}
	}
	
	@Override
	public double getValue(Map<String, Double> variables)
	{
		return variables.get(this._name); 
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		if ( withRespectTo.equals(this._name) )
			return Expression.one();
		return new Variable("d("+this._name+")/d("+withRespectTo+")");
	}
	
	@Override
	public void appendVariablesNames(Collection<String> names)
	{
		if ( ! names.contains(this._name) )
			names.add(this._name);
	}
}