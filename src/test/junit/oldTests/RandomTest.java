package test.junit.oldTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

//import linearAlgebra.Vector;
import utility.ExtraMath;

public class RandomTest {
	
	@Test
	public void test()
	{
		int count = (int) 1e6;
		// NormRand
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		double[] r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getNormRand();

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		double[] c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getNormRand();
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}

//		System.out.println(Vector.toString(r));
//		System.out.println(Vector.toString(c));
		
		// getUniRand
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getUniRand(0.0, 1.1);

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getUniRand(0.0, 1.1);
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// getUniRandDbl
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getUniRandDbl(0.0, 1.1);

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getUniRandDbl(0.0, 1.1);
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// getUniRandInt
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getUniRandInt(0, 4);

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getUniRandInt(0, 4);
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// getUniRandInt
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getUniRandInt(4);

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getUniRandInt(4);
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// Exp2Rand
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getExp2Rand();

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getExp2Rand();
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// getUniRandAngle
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getUniRandAngle();

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getUniRandAngle();
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// getUniRandDbl
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		r = new double[count];
		for (int i = 0; i < count; i++)
			r[i] = ExtraMath.getUniRandDbl();

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		c = new double[count];
		for (int i = 0; i < count; i++)
		{
			c[i] = ExtraMath.getUniRandDbl();
			assertEquals(String.valueOf(r[i]),String.valueOf(c[i]));
		}
		
		// getRandBool
		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		boolean[] b = new boolean[count];
		for (int i = 0; i < count; i++)
			b[i] = ExtraMath.getRandBool();

		ExtraMath.initialiseRandomNumberGenerator(2431682436L);
		
		boolean[] a = new boolean[count];
		for (int i = 0; i < count; i++)
		{
			a[i] = ExtraMath.getRandBool();
			assertEquals(String.valueOf(a[i]),String.valueOf(b[i]));
		}
	}

}
