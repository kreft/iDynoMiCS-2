package expression;

import java.util.Collection;
import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;

public abstract class ComponentBoolean extends Component {

	protected String _expr;
	

	protected Component _a, _b;

	protected ComponentBoolean _c, _d;
	

	public ComponentBoolean(Component a, Component b)
	{
		this._a = a;
		this._b = b;
		if( a instanceof ComponentBoolean )
			this._c = (ComponentBoolean) a;
		if( b instanceof ComponentBoolean )
			this._d = (ComponentBoolean) b;		
	}
	
	@Override
	public String getName()
	{
		return "("+ this._c.getName() + this._expr + this._d.getName() + ")";
	}
	
	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		String out = this._c.reportEvaluation(variables) + this._expr +
											this._d.reportEvaluation(variables);
		return out;
	}
	
	@Override
	public void appendVariablesNames(Collection<String> names)
	{
		this._c.appendVariablesNames(names);
		this._d.appendVariablesNames(names);
	}
	
	public abstract Boolean calculateBoolean(Map<String, Double> variables);
	
	protected double calculateValue(Map<String, Double> variables) 
	{
		if ( this.calculateBoolean(variables) )
			return 1.0;
		else
			return 0.0;
	}
	
	public Component differentiate(String withRespectTo) 
	{
		Log.out(Tier.CRITICAL, "Unable to differntiate logical expressions");
		return null;
	}

	
	/**
	 * \brief Helper method for sub-classes that may encounter infinite values.
	 * 
	 * @param variables Dictionary of variable names with associated values
	 * that triggered the infinite value.
	 */
	protected void infiniteValueWarning(Map<String, Double> variables)
	{
		Log.out(Log.Tier.CRITICAL,"WARNING! Infinite value: " + this.getName() + 
									" = " + this.reportEvaluation(variables));
	}
}
