package expression.logic;

import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.Component;
import expression.ComponentBoolean;

public class LogicOr extends ComponentBoolean {

	public LogicOr(Component a, Component b) 
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

}
