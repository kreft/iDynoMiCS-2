package expression.logic;

import java.util.Map;

import expression.Component;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicGreaterThan extends ComponentBoolean {

	public LogicGreaterThan(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = ">";
	}

	@Override
	public Boolean calculateBoolean(Map<String, Object> variables) 
	{
		
		return ( 1 == ( this._a.getValueEle(variables).compareTo( this._b.getValueEle(variables) ) ) );
	
	}	

}