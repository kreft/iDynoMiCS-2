package expression;

import java.util.Collection;
import java.util.Map;

import aspect.AspectInterface;

public abstract class ComponentBoolean extends Elemental {

	protected String _expr;
	
	protected Elemental _a, _b;

	protected ComponentBoolean _c, _d;
	

	public ComponentBoolean(Elemental a, Elemental b)
	{
		super(Type.bool);
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
		return "("+ this._a.getName() + this._expr + this._b.getName() + ")";
	}
	

	@Override
	public double getValue(Map<String, Double> variables) 
	{
		return (this.calculateBoolean(variables) ? 1.0 : 0.0);
	}	
	
	public abstract boolean booleanEvaluate(AspectInterface subject);
	
	public Object evaluate(AspectInterface subject)
	{
		return (this.booleanEvaluate(subject) ? 1.0 : 0.0);
	}

	@Override
	public void appendVariablesNames(Collection<String> names)
	{
		this._a.appendVariablesNames(names);
		this._b.appendVariablesNames(names);
	}
	
	public abstract Boolean calculateBoolean(Map<String, Double> variables);

}
