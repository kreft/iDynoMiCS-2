package testJUnit;

import java.util.HashMap;

import org.junit.Test;

import reaction.RateExpression;

public class RateExpressionTest {

	@Test
	public void express()
	{
//		RateExpression exp = new RateExpression("25.0 * 3.0", null);
//		RateExpression exp2 = new RateExpression("1.0 / (1.0 + 0.1)", null);
		RateExpression exp3 = new RateExpression("(-22.0 * (1.0 * (1.0 * 5.5)))", null);
		
//		System.out.println(exp.stringEval() + " = " + exp.component.getValue(new HashMap<String,Double>()));
//		System.out.println(exp2.stringEval() + " = " + exp2.component.getValue(new HashMap<String,Double>()));
		System.out.println(exp3.stringEval() + " = " + exp3.component.getValue(new HashMap<String,Double>()));
	}
}