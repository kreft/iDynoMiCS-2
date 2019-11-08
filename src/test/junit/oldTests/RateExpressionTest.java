package test.junit.oldTests;

import static dataIO.Log.Tier.DEBUG;
import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import java.util.HashMap;

import org.junit.Test;

import dataIO.Log;
import expression.Expression;
import test.OldTests;
import utility.ExtraMath;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class RateExpressionTest
{
	@Test
	public void shouldBuildCorrectExpressionsFromString()
	{
		OldTests.setupSimulatorForTest(1.0, 1.0, "ExpressionBtest");
		
		Expression expr;
		String str;
		HashMap<String,Double> vars = new HashMap<String,Double>();
		double calculated, correct;
		/*
		 * Start with a simple one.
		 */
		str = "25.0 * 3.0";
		correct = 25.0 * 3.0;
		expr = new Expression(str);
		//expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * A little more complicated.
		 */
		str = "#e^1.0 / 5.0EXP(1.0 + 0.5)";
		correct = Math.E / (5.0 * Math.pow(10.0,(1.0 + 0.5)));
		expr = new Expression(str);
		expr.printEval();
		Log.out(DEBUG, expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * Lots of terms.
		 */
		str = "(-22.0 * (1.0 * (1.0 * 5.5)) - 2.0) + 1.0 / (8.1 * 5.0)";
		correct = (-22.0 * (1.0 * (1.0 * 5.5)) - 2.0) + 1.0 / (8.1 * 5.0);
		expr = new Expression(str);
		//expr.printEval();
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
		expr = new Expression(str);
		//expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * Single constant, used to cause problems.
		 */
		str = "25.0";
		correct = 25.0;
		expr = new Expression(str);
		//expr.printEval();
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
		/*
		 * Single variable, used to cause problems.
		 */
		str = "mu";
		correct = vars.get("mu");
		expr = new Expression(str);
		System.out.println(expr.getName());
		System.out.println("");
		calculated = expr.getValue(vars);
		assertTrue(str, ExtraMath.areEqual(calculated, correct, TOLERANCE));
	}
}