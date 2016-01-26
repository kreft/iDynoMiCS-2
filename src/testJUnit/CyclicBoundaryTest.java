package testJUnit;

import java.util.LinkedList;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import shape.Shape;

public class CyclicBoundaryTest
{
	@Test
	public void numberOfCyclicPointsShouldBeCorrect()
	{
		/* Make the shape. */
		Shape aShape = (Shape) Shape.getNewInstance("rectangle");
		/* Set up the cyclic dimensions. */
		for ( String side : new String[] {"X", "Y"})
			aShape.makeCyclic(side);
		aShape.setDimensionLengths(new double[]{1.0, 1.0, 0.0});
		/* Check we get the correct number of cyclic points. */
		double[] location = new double[]{0.5, 0.5};
		LinkedList<double[]> cyclics = aShape.getCyclicPoints(location);
		System.out.println("#cyclics "+cyclics.size());
		assertEquals("#cyclics = 9", cyclics.size(), 9);
	}
}