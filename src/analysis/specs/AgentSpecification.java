package analysis.specs;

import aspect.AspectInterface;
import expression.Expression;
import utility.Helper;

public class AgentSpecification extends Specification {

	private Expression _exp;
	private String input;
	
	public AgentSpecification(String expression)
	{
		input = expression;
		if (Helper.expressionParseable(expression))
			this._exp = new Expression(expression);
	}
	
	public Object value(AspectInterface subject)
	{
		if(subject.isAspect(input))
		{
			Object obj = subject.getValue(input);
			if( obj instanceof String )
				return (String) obj;
			else if ( obj instanceof Expression )
			{
				Expression ex = (Expression) obj;
				return ex.evaluate(subject);
			}
			else
				return String.valueOf(obj);
		}
		return this._exp.evaluate(subject);
	}
	
	public String header()
	{
		return input;
	}
}
