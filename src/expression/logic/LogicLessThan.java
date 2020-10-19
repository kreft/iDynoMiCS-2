package expression.logic;

import java.util.Map;

import expression.Component;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicLessThan extends ComponentBoolean {

	public LogicLessThan(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = "<";
	}

	@Override
	public Boolean calculateBoolean(Map<String, Object> variables) 
	{
		return ( this._a.getValueEle(variables) < this._b.getValueEle(variables) );
	}	

}
