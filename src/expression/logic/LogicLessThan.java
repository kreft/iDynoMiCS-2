package expression.logic;

import java.util.Map;

import aspect.AspectInterface;
import expression.ComponentBoolean;
import expression.Elemental;

public class LogicLessThan extends ComponentBoolean {

	public LogicLessThan(Elemental a, Elemental b) 
	{		
		super(a, b);
		this._expr = "<";
	}

	@Override
	public Boolean calculateBoolean(Map<String, Double> variables) 
	{
		return ( this._a.getValue(variables) < this._b.getValue(variables) );
	}	

	@Override
	public boolean booleanEvaluate(AspectInterface subject) {
		return  (double) this._a.evaluate(subject) <
				 (double) this._b.evaluate(subject);
	}
}
