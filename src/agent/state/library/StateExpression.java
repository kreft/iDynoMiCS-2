package agent.state.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Calculated;
import expression.ExpressionBuilder;

/**
 * 
 * @author baco
 *
 */
public class StateExpression extends Calculated {
	
	private ExpressionBuilder e;

	@Override
	public void setInput(String input)
	{
		this.e = new ExpressionBuilder(input.replaceAll("\\s+",""));
	}
	
	/**
	 * input[0] expression
	 */
	public Object get(AspectInterface aspectOwner)
	{
		HashMap<String, Double> variables = new HashMap<String, Double>();
		for(String var : e._variables)
			variables.put(var, aspectOwner.getDouble(var));
		return e.component.getValue(variables);
	}

}