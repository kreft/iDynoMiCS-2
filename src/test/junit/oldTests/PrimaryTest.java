package test.junit.oldTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import linearAlgebra.Vector;

/**
 * be careful using Vector.xxxEquals operations
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class PrimaryTest {

	@Test
	public void illigalOperations()
	{
		double[] a = { 2.0, 2.0 };
		double[] b = a;
		Vector.timesEquals(b, 2.0);
		assertTrue("mis-usage of Vector.xxxEquals operations", a[0] == 4.0 );
	}
}
