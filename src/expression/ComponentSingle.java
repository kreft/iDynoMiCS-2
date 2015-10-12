package expression;

import java.util.HashMap;

public abstract class ComponentSingle extends Component
{
	protected String _expr = "";
	
	protected Component _a;
	
	public ComponentSingle(Component a)
	{
		this._a = a;
	}

	@Override
	public String getName()
	{
		String out = this._expr+"("+this._a.getName()+")";
		return ( isNegative() ) ? "-"+out : out;
	}

	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		String out = this._expr+"("+this._a.reportValue(variables)+")";
		return ( isNegative() ) ? "-"+out : out; 
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		if ( this._a instanceof Constant )
			return Expression.zero();
		return this.getDifferential(withRespectTo);
	}
	
	protected abstract Component getDifferential(String withRespectTo);
}