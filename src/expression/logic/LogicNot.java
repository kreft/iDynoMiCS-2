package expression.logic;

import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicNot extends ComponentBoolean {

	public LogicNot(Elemental a) 
	{		
		super(a, null);
		this._expr = "NOT";
		if( !(a instanceof ComponentBoolean) )
			Log.out(Tier.CRITICAL, "Must assign boolean components to " + 
					this._expr + " operator");
	}

	@Override
	public Boolean calculateBoolean(Map<String, Object> variables) 
	{
		return ( !this._c.calculateBoolean(variables) );
	}	

}