package testJUnit;

import static testJUnit.AllTests.TOLERANCE;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import utility.ExtraMath;

/**
 * \brief Set of tests for the ExtraMath utility class.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ExtraMathTest
{
	@Test
	public void floorModShouldBehave()
	{
		double calc, answ;
		/*
		 * 1.0 % 5.0
		 */
		calc = 1.0 % 5.0;
		answ = 1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(1.0, 5.0);
		answ = 1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		/*
		 * -1.0 % 5.0
		 */
		calc = -1.0 % 5.0;
		answ = -1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(-1.0, 5.0);
		answ = 4.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		/*
		 * 6.0 % 5.0
		 */
		calc = 6.0 % 5.0;
		answ = 1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(6.0, 5.0);
		answ = 1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		/*
		 * -6.0 % 5.0
		 */
		calc = -6.0 % 5.0;
		answ = -1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(-6.0, 5.0);
		answ = 4.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		/*
		 * -1.5 % 5.0
		 */
		calc = -1.5 % 5.0;
		answ = -1.5;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(-1.5, 5.0);
		answ = 3.5;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		/*
		 * -1.0 % -5.0
		 */
		calc = -1.0 % -5.0;
		answ = -1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(-1.0, -5.0);
		answ = -1.0;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		/*
		 * 1.5 % -5.2
		 */
		calc = 1.5 % -5.2;
		answ = 1.5;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
		calc = ExtraMath.floorMod(1.5, -5.2);
		answ = -3.7;
		assertTrue(ExtraMath.areEqual(calc, answ, TOLERANCE));
	}
}
