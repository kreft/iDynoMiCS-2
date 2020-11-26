/**
 * 
 */
package expression.arithmetic;

import java.util.Collection;
import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentSimple;

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
	protected double calculateValue(Map<String, Double> variables)
	{
		/* using 0.0 in cases were a variable is not defined is an easy default
		 * but might result in errors if carelessly used.. TODO reflect 
		Double out = variables.get(this._name); 
		if ( out == null )
			return 0.0;
		else
			return out; */
		return variables.get(this._name);
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		/* for undefined objects keep the name?
		 * makes string comparisons possible */
		Object out = subject.getValue(this._name);
		return (out == null ? this._name : out );
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		if ( withRespectTo.equals(this._name) )
			return Arithmetic.one();
		return new Variable("d("+this._name+")/d("+withRespectTo+")");
	}
	
	@Override
	public void appendVariablesNames(Collection<String> names)
	{
		if ( ! names.contains(this._name) )
			names.add(this._name);
	}
}