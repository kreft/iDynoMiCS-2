package testJUnit;

import org.junit.Test;

import reaction.RateExpression;

public class RateExpressionTest {

	@Test
	public void express()
	{
		RateExpression exp = new RateExpression("- mu * S / (S + 0.1)", null);
		exp.build();
		exp.printEval();
	}
}