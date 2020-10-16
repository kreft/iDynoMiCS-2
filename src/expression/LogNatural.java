/**
 * 
 */
package expression;

import java.util.Map;

/**
 * \brief Component of a mathematical expression that is the natural logartihm
 * of another component.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class LogNatural extends ComponentSingle
{
	/**
	 * \brief Construct a natural logarithm component of a mathematical
	 * expression from a sub-component.
	 * 
	 * @param a {@code Component} whose cosine will be evaluated.
	 */
	public LogNatural(ComponentNumerical a)
	{
		super(a);
		this._expr = "ln";
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		return Math.log(this._a.getValue(variables));
	}
	
	@Override
	protected ComponentNumerical getDifferential(String withRespectTo)
	{
		return new Division(this._a.differentiate(withRespectTo), this._a);
	}
}
