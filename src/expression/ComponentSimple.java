/**
 * 
 */
package expression;

import java.util.Map;

import expression.Elemental.Type;

/**
 * \brief The most basic component of a mathematical expression.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ComponentSimple extends Component
{
	/**
	 * Name of this component: used both for reporting and for evaluation of
	 * the entire mathematical expression.
	 */
	protected String _name;
	
	/**
	 * \brief Construct a component from its name.
	 * 
	 * @param name {@code String} name for this simple component.
	 */
	public ComponentSimple(String name)
	{
		super(Type.numeric);
		this._name = name;
	}
	
	@Override
	public String getName()
	{
		String out = this._name;
		return ( isNegative() ) ? "-"+out : out;
	}
	
	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		double out = this.getValue(variables);
		if ( isNegative() )
			if ( out < 0 )
				return Double.toString(-out);
			else
				return "-"+Double.toString(-out);
		return Double.toString(out);
	}
}
