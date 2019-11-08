/**
 * 
 */
package test.junit.oldTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.OldTests.TOLERANCE;

import org.junit.Test;

import linearAlgebra.Array;
import linearAlgebra.CholeskyDecomposition;
import linearAlgebra.EigenvalueDecomposition;
import linearAlgebra.LUDecomposition;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import utility.ExtraMath;

/**
 * \brief Set of tests for the linear algebra package.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class LinearAlgebraTest
{
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
		double[][] inverseA = Matrix.invert(a);
		/* Analytic solution */
		double[][] b = Matrix.zerosDbl(3);
		b[0][0] = -24.0; b[0][1] =  18.0; b[0][2] =  5.0;
		b[1][0] =  20.0; b[1][1] = -15.0; b[1][2] = -4.0;
		b[2][0] = - 5.0; b[2][1] =   4.0; b[2][2] = 1.0;
		/* Assert */
		assertTrue("m", Matrix.areSame(inverseA, b, TOLERANCE));
	}
	
	/**
	 * These exercises are from Thomas' Calculus (2005, 11th Edition)
	 * Pages 860, A59.
	 */
	@Test
	public void vectorExercises()
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
		assertTrue("Q1a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q1b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 2: -2 u */
		Vector.timesTo(componentForm, u, -2);
		w[0] = -6; w[1] = 4;
		norm = Math.sqrt(52);
		assertTrue("Q2a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q2b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 3: u + v */
		Vector.addTo(componentForm, u, v);
		w[0] = 1; w[1] = 3;
		norm = Math.sqrt(10);
		assertTrue("Q3a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q3b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 4: u - v */
		Vector.minusTo(componentForm, u, v);
		w[0] = 5; w[1] = -7;
		norm = Math.sqrt(74);
		assertTrue("Q4a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q4b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 5: 2u - 3v */
		Vector.timesTo(componentForm, u, 2);
		Vector.timesTo(temp, v, -3);
		Vector.addEquals(componentForm, temp);
		w[0] = 12; w[1] = -19;
		norm = Math.sqrt(505);
		assertTrue("Q5a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q5b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 6: -2v + 5v */
		Vector.timesTo(componentForm, u, -2);
		Vector.timesTo(temp, v, 5);
		Vector.addEquals(componentForm, temp);
		w[0] = -16; w[1] = 29;
		norm = Math.sqrt(1097);
		assertTrue("Q6a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q6b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 7: (3/5)u + (4/5)v */
		Vector.timesTo(componentForm, u, 3.0/5.0);
		Vector.timesTo(temp, v, 4.0/5.0);
		Vector.addEquals(componentForm, temp);
		w[0] = 0.2; w[1] = 14.0/5.0;
		norm = Math.sqrt(197)/5.0;
		assertTrue("Q7a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q7b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 8: -(5/13)u + (12/13)v */
		Vector.timesTo(componentForm, u, -5.0/13.0);
		Vector.timesTo(temp, v, 12.0/13.0);
		Vector.addEquals(componentForm, temp);
		w[0] = -3.0; w[1] = 70.0/13.0;
		norm = Math.sqrt(6421)/13.0;
		assertTrue("Q8a", Vector.areSame(componentForm, w, TOLERANCE));
		assertTrue("Q8b", ExtraMath.areEqual(Vector.normEuclid(componentForm),
															norm, TOLERANCE));
		/* Question 9: p->q where p = (1,3) and q = (2,-1). */
		int[] p = new int[]{1, 3};
		int[] q = new int[]{2, -1};
		int[] pq = Vector.minus(q, p);
		assertTrue("Q9", Vector.areSame(pq, new int[]{1, -4}));
		/* Question 10: origin->w where u is mid-point of (2,-1) and (-4,3). */
		u[0] = 2;  u[1] = -1;
		v[0] = -4; v[1] = 3;
		Vector.midPointTo(w, u, v);
		componentForm[0] = -1; componentForm[1] = 1;
		assertTrue("Q10", Vector.areSame(w, componentForm, TOLERANCE));
		/* Question 11: vector from (2, 3) to the origin. */
		p[0] = 2; p[1] = 3;
		Vector.reverseEquals(p);
		q[0] = -2; q[1] = -3;
		assertTrue("Q11", Vector.areSame(p, q));
		/* Question 12: u->v + p->q, 
		  		where u=(1,-1), v=(2,0), p=(-1,3), q=(-2,2). */
		u[0] = 1;  u[1] = -1;
		v[0] = 2;  v[1] = 0;
		p[0] = -1; p[1] = 3;
		q[0] = -2; q[1] = 2;
		w = Vector.toDbl(Vector.minus(q, p));
		Vector.addEquals(w, Vector.minus(v, u));
		componentForm[0] = 0; componentForm[1] = 0;
		assertTrue("Q12", Vector.areSame(w, componentForm, TOLERANCE));
		/* Question 13: unit vector with angle 2pi/3 to the positive x-axis. */
		v[0] = 1; v[1] = 2 * Math.PI / 3;
		u = Vector.unspherify(v);
		componentForm[0] = -0.5; componentForm[1] = Math.sqrt(3) / 2;
		assertTrue("Q13", Vector.areSame(u, componentForm, TOLERANCE));
		/* Question 14: unit vector with angle -3pi/4 to the positive x-axis.*/
		v[0] = 1; v[1] = -3 * Math.PI / 4;
		u = Vector.unspherify(v);
		componentForm[0] = -1/Math.sqrt(2); componentForm[1] = -1/Math.sqrt(2);
		assertTrue("Q14", Vector.areSame(u, componentForm, TOLERANCE));
		/*
		 * Questions 15 & 16 are about rotating points around the origin by a
		 * given angle.
		 */
		/* Question 17: p->q where p = (5,7,-1) and q = (2,9,-2). */
		p = new int[]{5, 7, -1};
		q = new int[]{2, 9, -2};
		pq = Vector.minus(q, p);
		assertTrue("Q17", Vector.areSame(pq, new int[]{-3, 2, -1}));
		/* Question 18: p->q where p = (1,2,0) and q = (-3,0,5). */
		p[0] = 1;  p[1] = 2; p[2] = 0;
		q[0] = -3; q[1] = 0; q[2] = 5;
		pq = Vector.minus(q, p);
		assertTrue("Q18", Vector.areSame(pq, new int[]{-4, -2, 5}));
		/* Question 19: u->v where u = (-7,-8,1) and v = (-10,8,1). */
		u = new double[]{-7, -8, 1};
		v = new double[]{-10, 8, 1};
		w = Vector.minus(v, u);
		assertTrue("Q19", Vector.areSame(w, new double[]{-3, 16, 0}));
		/* Question 20: u->v where u = (1,0,3) and v = (-1,4,5). */
		u[0] =  1; u[1] = 0; u[2] = 3;
		v[0] = -1; v[1] = 4; v[2] = 5;
		w = Vector.minus(v, u);
		assertTrue("Q20", Vector.areSame(w, new double[]{-2, 4, 2}));
	}
	
	@Test
	public void vectorFlip()
	{
		ExtraMath.initialiseRandomNumberGenerator();
		checkFlipDbl(100);
		checkFlipDbl(101);
		checkFlipInt(100);
		checkFlipInt(101);
	}
	
	private void checkFlipDbl(int nVar)
	{
		String oddEven = ((nVar%2) == 0) ? "even" : "odd";
		double[] u = Vector.randomZeroOne(nVar);
		double[] v = Vector.flip(u);
		assertFalse("Flipped not the same (double "+oddEven+")", Vector.areSame(u, v));
		double[] w = new double[nVar];
		Vector.flipTo(w, u);
		assertTrue("flipTo same as flip (double "+oddEven+")", Vector.areSame(v, w));
		Vector.flipEquals(v);
		assertTrue("Flip is reversible (double "+oddEven+")", Vector.areSame(u, v));
	}
	
	private void checkFlipInt(int nVar)
	{
		String oddEven = ((nVar%2) == 0) ? "even" : "odd";
		int[] u = Vector.randomInts(nVar, -10, 10);
		int[] v = Vector.flip(u);
		assertFalse("Flipped not the same (int "+oddEven+")", Vector.areSame(u, v));
		int[] w = new int[nVar];
		Vector.flipTo(w, u);
		assertTrue("flipTo same as flip (int "+oddEven+")", Vector.areSame(v, w));
		Vector.flipEquals(v);
		assertTrue("Flip is reversible (int "+oddEven+")", Vector.areSame(u, v));
	}
	
	@Test
	public void cartesianPolarExercises()
	{
		double[] cartesianOriginal, cartesianReturned, polarOriginal;
		/* **************** Cartesian -> polar -> Cartesian **************** */
		/* 1D
		 * Note that a negative input is nonsensical here.
		 */
		cartesianOriginal = new double[]{4.6};
		polarOriginal = Vector.spherify(cartesianOriginal);
		cartesianReturned = Vector.unspherify(polarOriginal);
		assertTrue("Cartesian -> Polar -> Cartesian (1D)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		/* 2D */
		cartesianOriginal = new double[]{-1.0, -2.0};
		polarOriginal = Vector.spherify(cartesianOriginal);
		cartesianReturned = Vector.unspherify(polarOriginal);
		assertTrue("Cartesian -> Polar -> Cartesian (2D)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		/* 3D */
		cartesianOriginal = new double[]{1.0, 2.0, 3.0};
		polarOriginal = Vector.spherify(cartesianOriginal);
		cartesianReturned = Vector.unspherify(polarOriginal);
		assertTrue("Cartesian -> Polar -> Cartesian (3D)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		
		
		
		polarOriginal = new double[]{Math.sqrt(2.0), 0.25*Math.PI};
		cartesianOriginal = new double[]{1.0, 1.0};
		cartesianReturned = Vector.unspherify(polarOriginal);
		assertTrue("pol(sqrt2,pi/4,0) -> car(1,1,0)",
				Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
	}
	
	@Test
	public void cylinderCartesianExercises()
	{
		double[] cartesianOriginal, cartesianReturned, cylindricalOriginal;
		/*
		 * Some 3D vector conversions where the outcome is known in advance.
		 */
		cylindricalOriginal = new double[]{1.0, Math.PI, 0.0};
		cartesianOriginal = new double[]{-1.0, 0.0, 0.0};
		cartesianReturned = Vector.uncylindrify(cylindricalOriginal);
		assertTrue("cyl(1,pi,0) -> car(-1,0,0)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		
		cylindricalOriginal[1] = 0.5 * Math.PI;
		cartesianOriginal[0] = 0.0; cartesianOriginal[1] = 1.0;
		cartesianReturned = Vector.uncylindrify(cylindricalOriginal);
		assertTrue("cyl(1,pi/2,0) -> car(0,1,0)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		
		cylindricalOriginal[1] = 1.5 * Math.PI;
		cartesianOriginal[0] = 0.0; cartesianOriginal[1] = -1.0;
		cartesianReturned = Vector.uncylindrify(cylindricalOriginal);
		assertTrue("cyl(1,3pi/2,0) -> car(0,-1,0)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		
		cylindricalOriginal[0] = Math.sqrt(2.0);
		cylindricalOriginal[1] = 0.25 * Math.PI;
		cartesianOriginal[0] = 1.0; cartesianOriginal[1] = 1.0;
		cartesianReturned = Vector.uncylindrify(cylindricalOriginal);
		assertTrue("cyl(sqrt2,pi/4,0) -> car(1,1,0)",
			Vector.areSame(cartesianOriginal, cartesianReturned, TOLERANCE));
		
		/*
		 * A bunch of randomly-generated vectors, in 1D, 2D and 3D.
		 */
		ExtraMath.initialiseRandomNumberGenerator();
		double[] cartOrig, cylindrical, cartCopy;
		for ( int i = 0; i < 10; i++ )
			for ( int nDim = 1; nDim <= 3; nDim++ )
			{
				cartOrig = Vector.randomZeroOne(nDim);
				cylindrical = Vector.cylindrify(cartOrig);
				cartCopy = Vector.uncylindrify(cylindrical);
				assertTrue(Vector.areSame(cartOrig, cartCopy, TOLERANCE));
			}
	}
	
	@Test
	public void readWriteString()
	{
		String str;
		/*
		 * Integer vector.
		 */
		int[] vIntOrig = Vector.randomInts(10, -10, 10);
		str = Vector.toString(vIntOrig);
		int[] vIntCopy = Vector.intFromString(str);
		assertTrue(Vector.areSame(vIntOrig, vIntCopy));
		/*
		 * Double vector.
		 */
		double[] vDblOrig = Vector.randomPlusMinus(10, 10.0);
		str = Vector.toString(vDblOrig);
		double[] vDblCopy = Vector.dblFromString(str);
		assertTrue(Vector.areSame(vDblOrig, vDblCopy));
		/*
		 * Integer matrix.
		 */
		int[][] mIntOrig = Matrix.randomInts(10, 10, -10, 10);
		str = Matrix.toString(mIntOrig);
		int[][] mIntCopy = Matrix.intFromString(str);
		assertTrue(Matrix.areSame(mIntOrig, mIntCopy));
		/*
		 * Double matrix.
		 */
		double[][] mDblOrig = Matrix.randomPlusMinus(10, 10, 10.0);
		str = Matrix.toString(mDblOrig);
		double[][] mDblCopy = Matrix.dblFromString(str);
		assertTrue(Matrix.areSame(mDblOrig, mDblCopy));
		/*
		 * Integer array.
		 */
		int[][][] aIntOrig = Array.randomInts(2, 2, 2, -10, 10);
		str = Array.toString(aIntOrig);
		int[][][] aIntCopy = Array.intFromString(str);
		assertTrue(Array.areSame(aIntOrig, aIntCopy));
		/*
		 * Double array.
		 */
		double[][][] aDblOrig = Array.randomPlusMinus(10, 10, 10, 10.0);
		str = Array.toString(aDblOrig);
		double[][][] aDblCopy = Array.dblFromString(str);
		assertTrue(Array.areSame(aDblOrig, aDblCopy));
	}
	
	@Test
	public void intSamplingIsUnique()
	{
		int[] sample;
		int n, min, max, count;
		/*
		 * Check when min = 0 and n < max.
		 */
		n = 5;
		min = 0;
		max = 6;
		sample = Vector.randomIntsNoReplacement(n, min, max);
		for ( int i = min; i < max; i++ )
		{
			count = Vector.countInstances(sample, i);
			assertTrue(count < 2);
		}
		/*
		 * Check when min != 0 and n < (max - min).
		 */
		n = 5;
		min = 3;
		max = 9;
		sample = Vector.randomIntsNoReplacement(n, min, max);
		for ( int i = min; i < max; i++ )
		{
			count = Vector.countInstances(sample, i);
			assertTrue(count < 2);
		}
		n = 5;
		min = -4;
		max = 2;
		sample = Vector.randomIntsNoReplacement(n, min, max);
		for ( int i = min; i < max; i++ )
		{
			count = Vector.countInstances(sample, i);
			assertTrue(count < 2);
		}
		/*
		 * Check when min = 0 and n = (max - min).
		 */
		n = 4;
		min = 2;
		max = 6;
		sample = Vector.randomIntsNoReplacement(n, min, max);
		for ( int i = min; i < max; i++ )
		{
			count = Vector.countInstances(sample, i);
			assertTrue(count < 2);
		}
		/*
		 * Check that it throws an exception when asked to do the impossible.
		 */
		n = 5;
		min = 0;
		max = 4;
		try
		{
			sample = Vector.randomIntsNoReplacement(n, min, max);
			fail("Managed to resample more times than possible");
		}
		catch ( IllegalArgumentException e)
		{
			
		}
	}
	
	@Test
	public void luDecomposition()
	{
		/*
		 * Decompose a matrix, then put it back again.
		 * 
		 * Example taken from
		 * http://nucinkis-lab.cc.ic.ac.uk/HELM/workbooks/workbook_30/30_3_lu_decomposition.pdf
		 */
		double[][] mOrig = new double[3][3];
		mOrig[0][0] = 1.0; mOrig[0][1] = 2.0; mOrig[0][2] = 4.0;
		mOrig[1][0] = 3.0; mOrig[1][1] = 8.0; mOrig[1][2] = 14.0;
		mOrig[2][0] = 2.0; mOrig[2][1] = 6.0; mOrig[2][2] = 13.0;
		LUDecomposition luD = new LUDecomposition(mOrig);
		double[][] l = luD.getL();
		double[][] u = luD.getU();
		int[] piv = luD.getPivot();
		double[][] mCopy = Matrix.zeros(mOrig);
		Matrix.reorderRowsTo(mCopy, Matrix.times(l, u), piv);
		assertTrue(Matrix.areSame(mOrig, mCopy, TOLERANCE));
		/*
		 * Solve a system of algebraic equations
		 */
		double[] b = new double[3];
		b[0] = 1.0; b[1] = 2.3; b[2] = 3.0;
		double[] x = luD.solve(b);
		double[] bCopy = Matrix.times(mOrig, x);
		assertTrue(Vector.areSame(b, bCopy, TOLERANCE));
	}
	
	@Test
	public void choleskyDecomposition()
	{
		ExtraMath.initialiseRandomNumberGenerator();
		/*
		 * Example taken from
		 * http://www.seas.ucla.edu/~vandenbe/103/lectures/chol.pdf
		 */
		double[][] a = new double[3][3];
		double[][] L = Matrix.zeros(a);
		a[0][0] = 25.0; a[0][1] = 15.0; a[0][2] = -5.0;
		a[1][0] = 15.0; a[1][1] = 18.0; a[1][2] =  0.0;
		a[2][0] = -5.0; a[2][1] =  0.0; a[2][2] = 11.0;
		L[0][0] =  5.0;
		L[1][0] =  3.0; L[1][1] =  3.0;
		L[2][0] = -1.0; L[2][1] =  1.0; L[2][2] =  3.0;
		
		CholeskyDecomposition cD = new CholeskyDecomposition(a);
		assertTrue(cD.isSymmetricPositiveDefinite());
		assertTrue(Matrix.areSame(L, cD.getL(), TOLERANCE));
		
//		double[][] b = Matrix.randomPlusMinus(3, 1, 10.0);
//		double[][] x = cD.solve(b);
//		double[][] D = Matrix.times(a, x);
//		assertTrue(Matrix.areSame(b, D, TOLERANCE));
		
		double[] b = Vector.randomPlusMinus(3, 10.0);
		double[] x = cD.solve(b);
		double[] D = Matrix.times(a, x);
		assertTrue(Vector.areSame(b, D, TOLERANCE));
	}
	
	@Test
	public void eigenvalueDecomposition()
	{
		// TODO more robust tests
		double[][] matrix = new double[3][3];
		matrix[0][0] = 1.0; matrix[0][1] = 2.0; matrix[0][2] = 4.0;
		matrix[1][0] = 3.0; matrix[1][1] = 8.0; matrix[1][2] = 14.0;
		matrix[2][0] = 2.0; matrix[2][1] = 6.0; matrix[2][2] = 13.0;
		EigenvalueDecomposition eD = new EigenvalueDecomposition(matrix);
		double[][] v = eD.getV();
		double[][] d = eD.getD();
		double[][] av = Matrix.times(matrix, v);
		double[][] vd = Matrix.times(v, d);
		assertTrue(Matrix.areSame(av, vd, TOLERANCE));
	}
}
