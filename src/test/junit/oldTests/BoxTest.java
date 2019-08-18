package test.junit.oldTests;

import org.junit.Test;

import surface.BoundingBox;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class BoxTest
{
	@Test
	public void box()
	{
		BoundingBox bob = new BoundingBox();
		bob.get(new double[][]{ {0.2, 0.2}, {0.3, 0.3 }}, 0.5, 0.0);
		System.out.println(bob.getReport());
	}
}
