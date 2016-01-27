package testJUnit;

import java.util.LinkedList;

import org.junit.Test;

import linearAlgebra.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
			int correct = 1;
			for ( int j = 0; j < i; j++ )
				correct *= 3;
			assertEquals("#cyclics = "+correct+" ("+i+"D)",
										cyclics.size(), correct);
		}
	}
}