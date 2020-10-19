package expression.logic;

import java.util.Map;

import expression.ComponentBoolean;
import expression.Elemental;

public class LogicEqual extends ComponentBoolean {
	

	public LogicEqual(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = "=";
	}

	@Override
	public Boolean calculateBoolean(Map<String, Object> variables) 
	{
		return ( this._a.getValueEle(variables).equals(this._b.getValueEle(variables) ) );
	}	

}