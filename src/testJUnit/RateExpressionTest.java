package testJUnit;

import java.util.HashMap;

import org.junit.Test;

import expression.ExpressionBuilder;

public class RateExpressionTest {

	@Test
	public void express()
	{
		ExpressionBuilder exp = new ExpressionBuilder("25.0 * 3.0", null);
		ExpressionBuilder exp2 = new ExpressionBuilder("1.0 / SQRT(1.0 + 0.5)", null);
		ExpressionBuilder exp3 = new ExpressionBuilder("(-22.0 * (1.0 * (1.0 * 5.5)) - 2.0) + 1.0 / (8.1 * 5.0)", null);
		ExpressionBuilder exp4 = new ExpressionBuilder("mu * S / (K + S)", null);
		
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