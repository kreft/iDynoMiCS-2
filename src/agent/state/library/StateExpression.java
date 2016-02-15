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

	/**
	 * input[0] expression
	 */
	public Object get(AspectInterface aspectOwner)
	{
		HashMap<String, Double> variables = new HashMap<String, Double>();
		ExpressionBuilder e = new ExpressionBuilder(input[0]);
		for(String var : e._variables)
			variables.put(var, aspectOwner.getDouble(var));
		return e.component.getValue(variables);
	}

}