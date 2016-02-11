package testJUnit;

import org.junit.Test;

import reaction.RateExpression;
import reaction.term.*;

public class RateExpressionTest {

	@Test
	public void express()
	{
		RateExpression exp = new RateExpression("- test * (test + 1)", null);
		exp.addTerm(new Monod(0.07), "test");
		exp.printEval();
	}
}
