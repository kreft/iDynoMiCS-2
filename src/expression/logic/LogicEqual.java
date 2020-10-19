package expression.logic;

import java.util.Map;

import aspect.AspectInterface;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicEqual extends ComponentBoolean {
	

	public LogicEqual(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = "=";
	}

	@Override
	public Boolean calculateBoolean(Map<String, Double> variables) 
	{
		return ( this._a.getValue(variables) == this._b.getValue(variables)  );
	}		
	
	@Override
	public boolean booleanEvaluate(AspectInterface subject) {
		return this._a.evaluate(subject).equals(this._b.evaluate(subject));
	}

}