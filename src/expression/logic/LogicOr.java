package expression.logic;

import java.util.Map;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicOr extends ComponentBoolean {

	public LogicOr(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = "OR";
		if( !(a instanceof ComponentBoolean) || 
				!(b instanceof ComponentBoolean) )
			Log.out(Tier.CRITICAL, "Must assign boolean components to " + 
					this._expr + " operator");
	}

	@Override
	public Boolean calculateBoolean(Map<String, Double> variables) 
	{
		return (this._c.calculateBoolean(variables) || 
				this._d.calculateBoolean(variables));
	}	
	
	@Override
	public boolean booleanEvaluate(AspectInterface subject) {
		return ( this._c.booleanEvaluate(subject) ||
				this._d.booleanEvaluate(subject) );
	}

}
