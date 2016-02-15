package testJUnit;

import java.util.HashMap;

import org.junit.Test;

import expression.ExpressionB;

public class RateExpressionTest {

	@Test
	public void express()
	{
		ExpressionB exp = new ExpressionB("25.0 * 3.0", null);
		ExpressionB exp2 = new ExpressionB("1.0 / SQRT(1.0 + 0.5)", null);
		ExpressionB exp3 = new ExpressionB("(-22.0 * (1.0 * (1.0 * 5.5)) - 2.0)"
				+ " + 1.0 / (8.1 * 5.0)", null);
		ExpressionB exp4 = new ExpressionB("mu * S / (K + S)", null);
		
		HashMap<String,Double> components = new HashMap<String,Double>();
		components.put("mu", 0.1);
		components.put("S", 3.0);
		components.put("K", 1.0);
		
		exp.printEval();
		System.out.println(exp.getName() + " = " + exp.getValue(
				new HashMap<String,Double>()));
		System.out.println();
		
		exp2.printEval();
		System.out.println(exp2.getName() + " = " + exp2.getValue(
				new HashMap<String,Double>()));
		System.out.println();
		
		exp3.printEval();
		System.out.println(exp3.getName() + " = " + exp3.getValue(
				new HashMap<String,Double>()));
		System.out.println();
		
		exp4.printEval();
		System.out.println(exp4.getName() + " = " + exp4.getValue(
				components));
	}
}