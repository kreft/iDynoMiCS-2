package testJUnit;

import org.junit.Test;

import surface.BoundingBox;

public class BoxTest {

	@Test
	public void box()
	{
		BoundingBox bob = new BoundingBox(new double[][]{ {0.2, 0.2}, {0.3, 0.3 }}, 0.5, 0.0);
		System.out.println(bob.getReport());

	}
}
