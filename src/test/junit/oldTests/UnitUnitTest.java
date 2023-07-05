package test.junit.oldTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import expression.Expression;
import expression.arithmetic.Unit;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class UnitUnitTest {

	@Test
	public void unitInterpretation()
	{
		Unit myUnit = new Unit();

		myUnit.fromString("g+1·m-1");
		System.out.println(myUnit.toString());
		assertTrue("correct unit conversion", myUnit.toString().contains("kg·m-1") || myUnit.toString().contains("m-1·kg"));
		
		System.out.println("\n");
		
		Unit unitA = new Unit();
		unitA.fromString("kg·cm-2");
		System.out.println("kg·cm-2 = " + unitA.toString());
		assertTrue("correct unit conversion", unitA.toString().contains("10000.0") );
		
		Unit unitB = new Unit();
		unitB.fromString("d");
		System.out.println("d = " + unitB.toString());
		assertTrue("correct unit conversion", unitB.toString().contains("86400") );
		
		
		Unit unitAB = Unit.product(unitA, unitB);
		System.out.println("product = " + unitAB );
		
		unitAB = Unit.quotient(unitA, unitB);
		System.out.println("quotient = " + unitAB + "\n");
		
		Expression expressiona = new Expression("35.0 *-2.0 ");
		double a = expressiona.getValue();

		Expression expressionb = new Expression("35.0 *-2.0 [g·dm-3]");
		double b = expressionb.getValue();

		System.out.println("no units no conversion " + a+ 
				" \nUnits, conversion to SI " + b );
		
		Unit unitC = new Unit();
		unitC.fromString("N");
		System.out.println("C (newton) = " + unitC);
		
		String format = " [mN]";
		System.out.println("C (formatted to mN) = " + unitC.toString(format));
		System.out.println("C (formatted to kg) = " + unitC.toString("kg"));

	}
	
}
