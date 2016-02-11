package testJUnit;

import org.junit.Test;

import reaction.RateExpression;

public class RateExpressionTest {

	@Test
	public void express()
	{
		RateExpression exp = new RateExpression("- mu * S / (S + 0.1) + 5.0 * (3.2 / 9.0)", null);
		exp.build();
		exp.printEval();
	}
}