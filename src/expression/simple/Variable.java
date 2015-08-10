/**
 * 
 */
package expression.simple;

import java.util.HashMap;

import expression.Component;
import expression.SimpleComponent;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class Variable extends SimpleComponent
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
	public Component getDifferential(String withRespectTo)
	{
		if ( withRespectTo.equals(this._name) )
			return Constant.one();
		return new Variable("d("+this._name+")/d("+withRespectTo+")");
	}
}