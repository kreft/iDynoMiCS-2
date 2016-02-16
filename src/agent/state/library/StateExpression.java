package agent.state.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Calculated;
import expression.ExpressionB;

/**
 * 
 * @author baco
 *
 */
public class StateExpression extends Calculated {
	
	private ExpressionB expression;
	private HashMap<String, Double> variables = new HashMap<String, Double>();

	@Override
	public void setInput(String input)
	{
		this.expression = new ExpressionB(input.replaceAll("\\s+",""));
	}
	
	/**
	 * input[0] expression
	 */
	public Object get(AspectInterface aspectOwner)
	{
		variables.clear();
		for(String var : expression._variables)
			variables.put(var, aspectOwner.getDouble(var));
		return expression.getValue(variables);
	}

}