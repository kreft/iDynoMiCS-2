package testJUnit;

import java.util.HashMap;

import org.junit.Test;

import reaction.RateExpression;

public class RateExpressionTest {

	@Test
	public void express()
	{
		RateExpression exp = new RateExpression("25.0 * 3.0", null);
		RateExpression exp2 = new RateExpression("1.0 / (1.0 + 0.5)", null);
		RateExpression exp3 = new RateExpression("(-22.0 * (1.0 * (1.0 * 5.5)))", null);
		RateExpression exp4 = new RateExpression("mu * S / (K + S)", null);
		
		HashMap<String,Double> components = new HashMap<String,Double>();
		components.put("mu", 0.1);
		components.put("S", 3.0);
		components.put("K", 1.0);
		
		System.out.println(exp.stringEval() + " = " + exp.component.getValue(
				new HashMap<String,Double>()));
		System.out.println(exp2.stringEval() + " = " + exp2.component.getValue(
				new HashMap<String,Double>()));
		System.out.println(exp3.stringEval() + " = " + exp3.component.getValue(
				new HashMap<String,Double>()));
		System.out.println(exp4.stringEval() + " = " + exp4.component.getValue(
				components));
	}
}