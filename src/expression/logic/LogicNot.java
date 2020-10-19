package expression.logic;

import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.Component;
import expression.ComponentBoolean;

public class LogicNot extends ComponentBoolean {

	public LogicNot(Component a) 
	{		
		super(a, null);
		this._expr = "NOT";
		if( !(a instanceof ComponentBoolean) )
			Log.out(Tier.CRITICAL, "Must assign boolean components to " + 
					this._expr + " operator");
	}

	@Override
	public Boolean calculateBoolean(Map<String, Double> variables) 
	{
		return ( !this._c.calculateBoolean(variables) );
	}	

}