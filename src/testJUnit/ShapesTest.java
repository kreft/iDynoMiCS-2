package testJUnit;

import java.util.Arrays;
import java.util.LinkedList;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static testJUnit.AllTests.TOLERANCE;
import linearAlgebra.Vector;

import shape.Shape;

public class ShapesTest
{
	@Test
	public void numberOfCyclicPointsShouldBeCorrect()
	{
		String[] shapeNames = new String[]{"line", "rectangle", "cuboid"};
		String[] sideNames = new String[] {"X", "Y", "Z"};
		for ( int i = 1; i < 4; i++ )
		{
			/* Make the shape. */
			Shape aShape = (Shape) Shape.getNewInstance(shapeNames[i-1]);
			/* Set up the cyclic dimensions. */
			for ( int dim = 0; dim < i; dim++ )
				aShape.makeCyclic(sideNames[dim]);
			aShape.setDimensionLengths(Vector.onesDbl(i));
			/* Check we get the correct number of cyclic points. */
			double[] location = Vector.vector(i, 0.5);
			LinkedList<double[]> cyclics = aShape.getCyclicPoints(location);
			/*
			 * The correct result is 3 to the power of the number of dimensions.
			 */
			int correct = 1;
			for ( int j = 0; j < i; j++ )
				correct *= 3;
			assertEquals("#cyclics = "+correct+" ("+i+"D)",
										cyclics.size(), correct);
		}
	}
	
	@Test
	public void shouldFindShortestDiff()
	{
		Shape aShape;
		double[] a, b, diff, correct;
		/*
		 * Rectangle with one cyclic dimension.
		 */
		aShape = (Shape) Shape.getNewInstance("rectangle");
		aShape.makeCyclic("X");
		aShape.setDimensionLengths(Vector.onesDbl(2));
		a = Vector.vector(2, 0.9);
		b = Vector.vector(2, 0.1);
		diff = aShape.getMinDifference(a, b);
		correct = new double[]{-0.2, 0.8};
		assertTrue("rectangle, 1 cyclic",
									Vector.areSame(correct, diff, TOLERANCE));
		/*
		 * Circle
		 */
		aShape = (Shape) Shape.getNewInstance("circle");
		aShape.makeCyclic("theta");
		aShape.setDimensionLengths(new double[]{2.0, 2*Math.PI, 0.0});
		a[0] = 1.0; a[1] = 1.0;
		b[0] = -1.0; b[1] = 1.0;
		diff = aShape.getMinDifference(a, b);
		System.out.println("diff: "+Arrays.toString(diff));
		
	}
}