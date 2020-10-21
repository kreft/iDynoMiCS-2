package analysis.specs;

import aspect.AspectInterface;
import expression.Expression;

public class AgentSpecification {

	private Expression _exp;
	
	public AgentSpecification(String expression)
	{
		this._exp = new Expression(expression);
	}
	
	public Object value(AspectInterface subject)
	{
		return this._exp.evaluate(subject);
	}
}
