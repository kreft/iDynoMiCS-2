package testJUnit;

import java.util.LinkedList;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import boundary.Boundary;
import boundary.BoundaryLibrary.SolidBoundary;
import dataIO.Log;
import static dataIO.Log.Tier.DEBUG;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import static testJUnit.AllTests.TOLERANCE;

import shape.Dimension.Dim;
import shape.Shape;
import shape.ShapeLibrary.Rectangle;
import shape.resolution.ResolutionCalculator.UniformResolution;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
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
		//System.out.println("diff: "+Vector.toString(diff));
		// FIXME
	}
	
	@Test
	public void shouldIterateCorrectly()
	{
		AllTests.setupSimulatorForTest(1.0, 1.0, "shapesShouldIterateProperly");
		int[][] trueNhb = new int[3][3];
		Dim[] dims = new Dim[]{Dim.X, Dim.Y};
		Shape shp = new Rectangle();
		UniformResolution resCalc = new UniformResolution();
		resCalc.setLength(3.0);
		resCalc.setResolution(1.0);
		for ( Dim d : dims )
			shp.setDimensionResolution(d, resCalc);
		/*
		 * Try first with solid boundaries.
		 */
		Boundary bndry = new SolidBoundary();
		for ( Dim d : dims )
			for ( int extreme = 0; extreme < 2; extreme++ )
				shp.setBoundary(d, extreme, bndry);
		/* Set up the array of true inside neighbor numbers. */
		trueNhb[0][0] = 2; trueNhb[0][1] = 3; trueNhb[0][2] = 2;
		trueNhb[1][0] = 3; trueNhb[1][1] = 4; trueNhb[1][2] = 3;
		trueNhb[2][0] = 2; trueNhb[2][1] = 3; trueNhb[2][2] = 2;
		/* Check it is correct. */
		Log.out(DEBUG, "Solid boundaries");
		checkIteration(shp, trueNhb);
		Log.out(DEBUG, "");
		/*
		 * Now try with cyclic dimensions.
		 */
		for ( Dim d : dims )
			shp.makeCyclic(d);
		Matrix.setAll(trueNhb, 4);
		Log.out(DEBUG, "Cyclic dimensions");
		checkIteration(shp, trueNhb);
		Log.out(DEBUG, "");
	}
	
	private void checkIteration(Shape shp, int[][] trueNhb)
	{
		int iterCount, nhbCount;
		int[] coord;
		iterCount = 0;
		for ( shp.resetIterator(); shp.isIteratorValid(); shp.iteratorNext() )
		{
			iterCount++;
			nhbCount = 0;
			for ( shp.resetNbhIterator();
					shp.isNbhIteratorValid(); shp.nbhIteratorNext() )
			{
				if ( shp.isNhbIteratorInside() )
					nhbCount++;
			}
			coord = shp.iteratorCurrent();
			Log.out(DEBUG, "Coord "+Vector.toString(coord)+" has "+nhbCount+" neighbors");
			assertEquals(nhbCount, trueNhb[coord[0]][coord[1]]);
		}
		assertEquals(iterCount, 9);
	}
}