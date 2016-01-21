/**
 * 
 */
package testJUnit;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import utility.ExtraMath;

/**
 * @author cleggrj
 *
 */
public class LinearAlgebraTest
{
	double TOLERANCE = 1E-6;
	
	@Test
	public void multiplicationOfZeroVectorShouldReturnZero()
	{
		assertEquals("vector of ones dot vector of zeros should be zero (int)",
				  Vector.dotProduct(Vector.onesInt(3), Vector.zerosInt(3)), 0);
		assertEquals("vector of ones dot vector of zeros should be zero (dbl)",
				 Vector.dotProduct(Vector.onesDbl(3), Vector.zerosDbl(3)), 0.0,
				 TOLERANCE);
		
	}
	
	
	@Test
	public void matrixMultiplicationShouldBeCorrect()
	{
		/* Test matrix */
		double[][] a = Matrix.zerosDbl(3);
		a[0][0] = 1.0; a[0][1] = 2.0; a[0][2] = 3.0;
		a[1][0] = 0.0; a[1][1] = 1.0; a[1][2] = 4.0;
		a[2][0] = 5.0; a[2][1] = 6.0; a[2][2] = 0.0;
		/* Analytic solution */
		double[][] b = Matrix.zerosDbl(3);
		b[0][0] = -24.0; b[0][1] =  18.0; b[0][2] =  5.0;
		b[1][0] =  20.0; b[1][1] = -15.0; b[1][2] = -4.0;
		b[2][0] = - 5.0; b[2][1] =   4.0; b[2][2] = 1.0;
		/* Assert */
		assertTrue("m", Matrix.areSame(Matrix.invert(a), b, TOLERANCE));
	}
	
	/**
	 * These exercises are from Thomas' Calculus (2005, 11th Edition)
	 * Pages 860-862, A59-60.
	 */
	@Test
	public void vectorExcercises()
	{
		double[] u = new double[]{3, -2};
		double[] v = new double[]{-2, 5};
		double[] componentForm = new double[2];
		double[] temp = new double[2];
		double[] w = new double[2];
		double norm;
		/* Question 1: 3 u */
		Vector.timesTo(componentForm, u, 3);
		w[0] = 9; w[1] = -6;
		norm = 3 * Math.sqrt(13);
		assertTrue("1a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("1b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 2: -2 u */
		Vector.timesTo(componentForm, u, -2);
		w[0] = -6; w[1] = 4;
		norm = Math.sqrt(52);
		assertTrue("2a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("2b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 3: u + v */
		Vector.addTo(componentForm, u, v);
		w[0] = 1; w[1] = 3;
		norm = Math.sqrt(10);
		assertTrue("3a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("3b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 4: u - v */
		Vector.minusTo(componentForm, u, v);
		w[0] = 5; w[1] = -7;
		norm = Math.sqrt(74);
		assertTrue("4a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("4b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 5: 2u - 3v */
		Vector.timesTo(componentForm, u, 2);
		Vector.timesTo(temp, v, -3);
		Vector.addEquals(componentForm, temp);
		w[0] = 12; w[1] = -19;
		norm = Math.sqrt(505);
		assertTrue("5a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("5b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 6: -2v + 5v */
		Vector.timesTo(componentForm, u, -2);
		Vector.timesTo(temp, v, 5);
		Vector.addEquals(componentForm, temp);
		w[0] = -16; w[1] = 29;
		norm = Math.sqrt(1097);
		assertTrue("6a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("6b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 7: (3/5)u + (4/5)v */
		Vector.timesTo(componentForm, u, 3.0/5.0);
		Vector.timesTo(temp, v, 4.0/5.0);
		Vector.addEquals(componentForm, temp);
		w[0] = 0.2; w[1] = 14.0/5.0;
		norm = Math.sqrt(197)/5.0;
		assertTrue("7a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("7b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 8: -(5/13)u + (12/13)v */
		Vector.timesTo(componentForm, u, -5.0/13.0);
		Vector.timesTo(temp, v, 12.0/13.0);
		Vector.addEquals(componentForm, temp);
		w[0] = -3.0; w[1] = 70.0/13.0;
		norm = Math.sqrt(6421)/13.0;
		assertTrue("8a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("8b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		
	}
	
}
