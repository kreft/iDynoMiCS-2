/**
 * 
 */
package expression;

import java.util.HashMap;
import java.util.List;

/**
 * \brief A component of a mathematical expression whose value is fixed.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Constant extends ComponentSimple
{
	/**
	 * The real number value this constant takes.
	 */
	protected double _value;
	
	/**
	 * \brief Construct a component of a mathematical expression that is
	 * constant.
	 * 
	 * @param name {@code String} name for this constant.
	 * @param value Real number value for this constant.
	 */
	public Constant(String name, double value)
	{
		super(name);
		this._value = value;
	}
	
	@Override
	public String reportEvaluation(HashMap<String, Double> variables)
	{
		return Double.toString(this._value);
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._value;
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		return ( this._name.equals(withRespectTo) ) ? 
								Expression.one() : Expression.zero();
	}
	
	@Override
	public void appendVariablesNames(List<String> names)
	{
		/* Do nothing! */
	}
}