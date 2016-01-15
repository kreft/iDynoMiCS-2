/**
 * 
 */
package testJUnit;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;

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
	
}
