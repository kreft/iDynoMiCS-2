/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Variable extends ComponentSimple
{
	
	public Variable(String name)
	{
		super(name);
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		return Double.toString(this.getValue(variables));
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
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
}