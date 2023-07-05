package test.junit.oldTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.OldTests.TOLERANCE;

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
	
	@Test
	public void overlapShouldBehave()
	{
		double calc;
		/* Simple. */ 
		calc = ExtraMath.overlap(1.0, 3.0, 2.0, 4.0);
		assertTrue(ExtraMath.areEqual(1.0, calc, TOLERANCE));
		calc = ExtraMath.overlap(2.0, 3.0, 2.0, 4.0);
		assertTrue(ExtraMath.areEqual(1.0, calc, TOLERANCE));
		calc = ExtraMath.overlap(0.0, 1.5, 0.0, 2.7);
		assertTrue(ExtraMath.areEqual(1.5, calc, TOLERANCE));
		/* Negatives. */
		calc = ExtraMath.overlap(-3.4, -1.6, -5.0, 4.0);
		assertTrue(ExtraMath.areEqual(1.8, calc, TOLERANCE));
		/* No overlap. */
		calc = ExtraMath.overlap(1.0, 2.0, 3.0, 4.0);
		assertTrue(ExtraMath.areEqual(0.0, calc, TOLERANCE));
		/* Illegal arguments. */
		try
		{
			calc = ExtraMath.overlap(2.0, 1.0, 3.0, 4.0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals(e.getMessage(), "Minimum > Maximum!");
		}
		try
		{
			calc = ExtraMath.overlap(1.0, 2.0, 4.0, 3.0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals(e.getMessage(), "Minimum > Maximum!");
		}
	}
	
	@Test
	public void shapeAreasShouldBeCorrect()
	{
		double calculated, answer;
		/* No segment */
		calculated = ExtraMath.areaOfACircleSegment(1.0, 0.0);
		answer = 0.0;
		assertTrue(ExtraMath.areEqual(answer, calculated, TOLERANCE));
		/* Semi-circle */
		calculated = ExtraMath.areaOfACircleSegment(1.0, Math.PI);
		System.out.println(calculated);
		answer = 0.5 * Math.PI;
		assertTrue(ExtraMath.areEqual(answer, calculated, TOLERANCE));
		/* Semi-circle */
		calculated = ExtraMath.areaOfACircleSegment(1.0, -Math.PI);
		answer = 0.5 * Math.PI;
		assertTrue(ExtraMath.areEqual(answer, calculated, TOLERANCE));
	}
}
