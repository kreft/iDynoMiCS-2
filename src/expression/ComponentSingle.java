package expression;

import java.util.Collection;
import java.util.Map;

import expression.Elemental.Type;
import expression.arithmetic.Arithmetic;
import expression.arithmetic.Constant;

/**
 * \brief Component of a mathematical expression that is a function of one
 * other component.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ComponentSingle extends Component
{
	/**
	 * {@code String} description of the function used. 
	 */
	protected String _expr;
	/**
	 * The sub-component.
	 */
	protected Component _a;
	
	/**
	 * \brief Construct a component of a mathematical expression from a single
	 * sub-component.
	 * 
	 * @param a The sub-component.
	 */
	public ComponentSingle(Component a)
	{
		super(Type.numeric);
		this._a = a;
	}

	@Override
	public String getName()
	{
		String out = this._expr+"("+this._a.getName()+")";
		return ( isNegative() ) ? "-"+out : out;
	}

	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		String out = this._expr+"("+this._a.reportEvaluation(variables)+")";
		return ( isNegative() ) ? "-"+out : out; 
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		if ( this._a instanceof Constant )
			return Arithmetic.zero();
		return this.getDifferential(withRespectTo);
	}
	
	protected abstract Component getDifferential(String withRespectTo);
	
	public void appendVariablesNames(Collection<String> names)
	{
		this._a.appendVariablesNames(names);
	}
}