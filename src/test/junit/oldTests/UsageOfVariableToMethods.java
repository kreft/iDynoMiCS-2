package test.junit.oldTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import linearAlgebra.Vector;

/**
 * Created this unit test to demonstrate the difference in Java primitive
 * assignment and object assignment double[] a = b "changes" a within the scope 
 * of the method but leaves the a in testDoubles unchanged. When we assign the
 * primitives within a specifically the object is changed and thus it can now
 * also be used outside of the method.
 *  
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class UsageOfVariableToMethods {
	
	@Test
	public void testDoubles()
	{
		double[] a = new double[] { 0.0 };
		double[] b = new double[] { 1.0 };
		wrongDoubleTo(a,b);
		System.out.println(Vector.toString(a));
		assertEquals(Vector.toString(a),"0.0");
		
		a = new double[] { 0.0 };
		b = new double[] { 1.0 };
		correctDoubleTo(a,b);
		System.out.println(Vector.toString(a));
		assertEquals(Vector.toString(a),"1.0");
		
	}
	
	public void wrongDoubleTo(double[] a, double[] b)
	{
		a = b;
	}
	
	public static void correctDoubleTo(double[] a, double[] b)
	{
		for (int i = 0; i < b.length; i++)
			a[i] = b[i];
	}


}
