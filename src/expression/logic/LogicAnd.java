package expression.logic;

import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicAnd extends ComponentBoolean {

	public LogicAnd(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = "AND";
		if( !(a instanceof ComponentBoolean) || 
				!(b instanceof ComponentBoolean) )
			Log.out(Tier.CRITICAL, "Must assign boolean components to " + 
					this._expr + " operator");
	}

	@Override
	public Boolean calculateBoolean(Map<String, Object> variables) 
	{
		return (this._c.calculateBoolean(variables) && 
				this._d.calculateBoolean(variables));
	}

}
