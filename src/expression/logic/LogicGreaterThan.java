package expression.logic;

import java.util.Map;

import expression.Component;
import expression.ComponentBoolean;

public class LogicGreaterThan extends ComponentBoolean {

	public LogicGreaterThan(Component a, Component b) 
	{		
		super(a, b);
		this._expr = ">";
	}

	@Override
	public Boolean calculateBoolean(Map<String, Double> variables) 
	{
		return ( this._a.getValue(variables) > this._b.getValue(variables) );
	}	

}