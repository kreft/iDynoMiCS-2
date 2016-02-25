package testJUnit;

import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import expression.ExpressionB;
import utility.ExtraMath;

import static testJUnit.AllTests.TOLERANCE;

/**
 * 
 * 
 */
public class RateExpressionTest
{
	
	@Test
	public void shouldBuildCorrectExpressionsFromString()
	{
		ExpressionB expr;
		String str;
		HashMap<String,Double> vars = new HashMap<String,Double>();
		double calculated, correct;
		/*
		 * Start with a simple one.
		 */
		str = "25.0 * 3.0";
		correct = 25.0 * 3.0;
		expr = new ExpressionB(str);
		expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * A little more complicated.
		 */
		str = "#e^1.0 / 5.0EXP(1.0 + 0.5)";
		correct = Math.E / (5.0 * Math.pow(10.0,(1.0 + 0.5)));
		expr = new ExpressionB(str);
		expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * Lots of terms.
		 */
		str = "(-22.0 * (1.0 * (1.0 * 5.5)) - 2.0) + 1.0 / (8.1 * 5.0)";
		correct = (-22.0 * (1.0 * (1.0 * 5.5)) - 2.0) + 1.0 / (8.1 * 5.0);
		expr = new ExpressionB(str);
		expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * Simple Michaelis-Menten kinetic.
		 */
		vars.put("mu", 0.1);
		vars.put("S", 3.0);
		vars.put("K", 1.0);
		str = "mu * S / (K + S)";
		correct = 0.1 * 3.0 / (1.0 + 3.0);
		expr = new ExpressionB(str);
		expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
	}
}