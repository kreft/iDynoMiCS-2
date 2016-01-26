package testJUnit;

import java.util.LinkedList;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import boundary.BoundaryCyclic;
import shape.BoundarySide;
import shape.Shape;

public class CyclicBoundaryTest
{
	@Test
	public void numberOfCyclicPointsShouldBeCorrect()
	{
		/* Make the shape. */
		Shape aShape = (Shape) Shape.getNewInstance("rectangle");
		aShape.setSideLengths(new double[]{1.0, 1.0, 0.0});
		/* Set up the cyclic boundaries. */
		BoundaryCyclic bMin, bMax;
		for ( String side : new String[] {"x", "y"})
		{
			bMin = new BoundaryCyclic();
			bMax = new BoundaryCyclic();
			bMin.setPartnerBoundary(bMax);
			bMax.setPartnerBoundary(bMin);
			aShape.addBoundary(BoundarySide.getSideFor(side+"min"), bMin);
			aShape.addBoundary(BoundarySide.getSideFor(side+"max"), bMax);
		}
		/* Check we get the correct number of cyclic points. */
		double[] location = new double[]{0.5, 0.5};
		LinkedList<double[]> cyclics = aShape.getCyclicPoints(location);
		System.out.println("#cyclics "+cyclics.size());
		assertEquals("#cyclics = 9", cyclics.size(), 9);
	}
}