package test.junit;

import java.util.LinkedList;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static test.AllTests.TOLERANCE;

import boundary.spatialLibrary.SolidBoundary;
import dataIO.Log;

import static dataIO.Log.Tier.DEBUG;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import shape.Shape;
import shape.CartesianShape;
import shape.Dimension.DimName;
import shape.ShapeLibrary.Circle;
import shape.ShapeLibrary.Rectangle;
import shape.ShapeLibrary.Sphere;
import shape.resolution.UniformResolution;
import test.AllTests;
import utility.ExtraMath;

/**
 * \brief Test class to check that {@code Shape} objects are behaving
 * themselves.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 * 		Friedrich-Schiller University Jena, Germany 
 */
public class ShapesTest
{
	/**
	 * Cyclic points are used by an AgentContainer's SpatialRegistry: they
	 * reproduce the point given as "ghost" points on either side of a cyclic
	 * dimension. The List of points produced includes the original.
	 */
	@Test
	public void numberOfCyclicPointsShouldBeCorrect()
	{
		/*
		 * Cartesian (Line, Rectangle, Cuboid)
		 */
		String[] shapeNames = new String[]{"Line", "Rectangle", "Cuboid"};
		String[] sideNames = new String[] {"X", "Y", "Z"};
		Shape aShape = null;
		LinkedList<double[]> cyclics;
		int correct;
		for ( int i = 1; i < 4; i++ )
		{
			/* Make the shape. */
			aShape = AllTests.GetShape(shapeNames[i-1]);
			/* Set up the cyclic dimensions. */
			for ( int dim = 0; dim < i; dim++ )
				aShape.makeCyclic(sideNames[dim]);
			aShape.setDimensionLengths(Vector.onesDbl(i));
			/* Check we get the correct number of cyclic points. */
			double[] location = Vector.vector(i, 0.5);
			cyclics = aShape.getCyclicPoints(location);
			/*
			 * The correct result is 3 to the power of the number of dimensions.
			 */
			correct = 1;
			for ( int j = 0; j < i; j++ )
				correct *= 3;
			assertEquals("#cyclics = "+correct+" ("+i+"D)",
										cyclics.size(), correct);
		}
		/*
		 * Circle
		 */
		correct = 3;
		aShape = AllTests.GetShape("Circle");
		aShape.makeCyclic("THETA");
		aShape.setDimensionLengths(new double[]{1, Math.PI / 2});
		/* lets take local coord (0.5, pi/4) ~ (0.3535, 0.3535) global. */
		cyclics = aShape.getCyclicPoints(Vector.vector(2, 0.3535));
		assertEquals("#cyclics = "+correct+" ("+2+"D)",
				cyclics.size(), correct);
		/*
		 * Cylinder
		 */
		correct = 9;
		aShape = AllTests.GetShape("Cylinder");
		aShape.makeCyclic("THETA");
		aShape.makeCyclic("Z");
		aShape.setDimensionLengths(new double[]{1, Math.PI / 2, 1});
		/* local coord (0.5, pi/4, 0.5) ~ (0.3535, 0.3535, 0.5) global. */
		cyclics = aShape.getCyclicPoints(new double[]{0.3535, 0.3535, 0.5});
		assertEquals("#cyclics = "+correct+" ("+3+"D)",
				cyclics.size(), correct);
		/*
		 * Sphere
		 */
		correct = 3;
		aShape = AllTests.GetShape("Sphere");
		aShape.makeCyclic("THETA");
		aShape.setDimensionLengths(new double[]{1, Math.PI / 2, Math.PI / 2});
		/* local coord (0.5, pi/4, pi/4) ~ (0.25, 0.25, 0.3535) global. */
		cyclics = aShape.getCyclicPoints(new double[]{0.25, 0.25, 0.3535});
		assertEquals("#cyclics = "+correct+" ("+3+"D)",
				cyclics.size(), correct);
	}
	
	@Test
	public void shouldFindShortestDiff()
	{
		Shape aShape = null;
		double[] a, b, diff, correct;
		/*
		 * Rectangle with one cyclic dimension.
		 */
		try {
			aShape = (Shape) Class.forName("shape.ShapeLibrary$Rectangle").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		aShape.makeCyclic("X");
		aShape.setDimensionLengths(Vector.onesDbl(2));
		a = Vector.vector(2, 0.9);
		b = Vector.vector(2, 0.1);
		diff = aShape.getMinDifferenceVector(a, b);
		correct = new double[]{-0.2, 0.8};
		assertTrue("rectangle, 1 cyclic",
									Vector.areSame(correct, diff, TOLERANCE));
		/*
		 * Circle
		 */
		try {
			aShape = (Shape) Class.forName("shape.ShapeLibrary$Circle").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		aShape.makeCyclic("theta");
		aShape.setDimensionLengths(new double[]{2.0, 2*Math.PI, 0.0});
		a[0] = 1.0; a[1] = 1.0;
		b[0] = -1.0; b[1] = 1.0;
		diff = aShape.getMinDifferenceVector(a, b);
		//System.out.println("diff: "+Vector.toString(diff));
		// FIXME
	}
	
	@Test
	public void shouldIterateCorrectly()
	{
		AllTests.setupSimulatorForTest(1.0, 1.0, "shapesShouldIterateProperly");
		int[][] trueNhb = new int[3][3];
		DimName[] dims = new DimName[]{DimName.X, DimName.Y};
		Shape shp = new Rectangle();
		UniformResolution resCalc = new UniformResolution();
		resCalc.setExtremes(0.0, 3.0);
		resCalc.setResolution(1.0);
		for ( DimName d : dims )
			shp.setDimensionResolution(d, resCalc);
		/*
		 * Try first with solid boundaries.
		 */
		for ( DimName d : dims )
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				SolidBoundary bndry = new SolidBoundary(d, extreme);
				shp.setBoundary(d, extreme, bndry);
			}
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
		for ( DimName d : dims )
			shp.makeCyclic(d);
		Matrix.setAll(trueNhb, 4);
		Log.out(DEBUG, "Cyclic dimensions");
		checkIteration(shp, trueNhb);
		Log.out(DEBUG, "");	
	}
	
	/**
	 * Checks several sample-points of a circle with theta-length 120°.
	 * See 'Docs/PolarShapes/Neighbours_Circle' for a drawing of a circle
	 * with coordinates and true neighbors indicated.
	 */
	@Test
	public void circeShouldIterateCorrectly()
	{
		/* solid boundaries */
		int[][] coords = new int[][]{ {0,0,0}, {1,0,0}, {1,1,0}, {2,3,0} };
		int[][][] trueNhb = new int[][][]{
			/* current sample (0, 0) */
			{ {1,0,0}, {1,1,0}, {1,2,0} }, 				
			/* current sample (1, 0) */
			{ {0,0,0}, {1,1,0}, {2,0,0}, {2,1,0} }, 		
			/* current sample (1, 1) */
			{ {0,0,0}, {1,0,0}, {1,2,0}, {2,1,0}, {2,2,0}, {2,3,0} }, 	
			/* current sample (2, 3) */
			{ {1,1,0}, {1,2,0}, {2,2,0}, {2,4,0} }, 		
		};
		DimName[] dims = new DimName[]{DimName.R, DimName.THETA};
		Shape shp = new Circle();
		
		/* r-dimension */
		UniformResolution resCalc = new UniformResolution();
		resCalc.setExtremes(0.0, 3.0);
		resCalc.setResolution(1.0);
		shp.setDimensionResolution(dims[0], resCalc);
		
		/* theta-dimension */
		resCalc = new UniformResolution(); 
		resCalc.setExtremes(0.0, 2 * Math.PI / 3);
		resCalc.setResolution(1.0);
		shp.setDimensionResolution(dims[1], resCalc);
		/*
		 * Try first with solid boundaries.
		 */
		for ( DimName d : dims )
			for ( int extreme = 0; extreme < 2; extreme++ )
				shp.setBoundary(d, extreme, new SolidBoundary(d, extreme));

		/* Check it is correct. */
		Log.out(DEBUG, "Solid boundaries");
		/* circle with theta length 2 * pi / 3 and res 1 has 9 voxels in total */
		checkIterationSamples(shp, coords, trueNhb, 9);
		Log.out(DEBUG, "");
		/*
		 * Now try with cyclic dimensions.
		 */
		shp.makeCyclic(dims[1]); /* only theta can be cyclic in the circle */
		trueNhb = new int[][][]{
			/* current sample (0, 0) */
			{{0,0,0}, {0,0,0}, {1,0,0}, {1,1,0}, {1,2,0}}, 	 
			 /* current sample (1, 0) */
			{{0,0,0}, {1,2,0}, {1,1,0}, {2,0,0}, {2,1,0}}, 	  
			/* current sample (1, 1) */
			{{0,0,0}, {1,0,0}, {1,2,0}, {2,1,0}, {2,2,0}, {2,3,0}},
			/* current sample (2, 3) */
			{{1,1,0}, {1,2,0}, {2,2,0}, {2,4,0}}, 		 	   
		};
		Log.out(DEBUG, "Cyclic dimensions");
		/* circle with length 2 * pi / 3 and res 1 has 9 voxels in total */
		checkIterationSamples(shp, coords, trueNhb, 9);
		Log.out(DEBUG, "");	
	}
	
	/**
	 * Checks several sample-points of a sphere with theta- and phi-length 90°.
	 * See 'Docs/PolarShapes/Neighbours_Sphere' for a drawing of a sphere
	 * with coordinates and true neighbors indicated.
	 */
	@Test
	public void sphereShouldIterateCorrectly()
	{
		AllTests.setupSimulatorForTest(1.0, 1.0, "sphereShouldIterateCorrectly");
		/* solid boundaries */
		int[][] coords = new int[][]{ {0,0,0}, {1,0,0}, {1,1,0}, {2,2,1} };
		int[][][] trueNhb = new int[][][]{
			/* current sample (0,0,0) */
			{ {1,0,0}, {1,1,0}, {1,1,1}, {1,2,0}, {1,2,1} }, 
			/* current sample (1,0,0) */
			{ {0,0,0}, {1,1,0}, {1,1,1}, {2,0,0}, {2,1,0}, {2,1,1} }, 	
			/* current sample (1,1,0) */
			{ {0,0,0}, {1,0,0}, {1,1,1}, {1,2,0}, {2,1,0}, {2,2,0},
			  {2,2,1}, {2,3,0}, {2,3,1} },
			/* current sample (2,2,1) */
			{ {1,1,0}, {1,1,1}, {2,1,0}, {2,1,1}, {2,2,0}, {2,2,2}, {2,3,1},
			  {2,3,2} }, 		
		};
		double[][] trueArea = new double[][]{
			/* current sample (0,0,0) */
			{ 0.210447, 0.287476, 0.287476, 0.392699, 0.392699 }, 
			/* current sample (1,0,0) */
			{ 0.210447, 0.916298, 0.916298, 0.307521, 0.267133, 0.267133 }, 	
			/* current sample (1,1,0) */
			{ 0.287476, 0.916298, 0.854059, 1.58707, 0.179097, 0.463347,
			  0.231673, 0.137893, 0.137893 },
			/* current sample (2,2,1) */
			{ 0.231673, 0.231673, 0.974585, 0.974585, 1.40113, 1.40113, 1.3414,
			  1.3414 }, 		
		};
		DimName[] dims = new DimName[]{DimName.R, DimName.PHI, DimName.THETA};
		Shape shp = new Sphere();
		
		/* r-dimension */
		UniformResolution resCalc = new UniformResolution();
		resCalc.setExtremes(0.0, 3.0);
		resCalc.setResolution(1.0);
		shp.setDimensionResolution(dims[0], resCalc);
		
		/* polar dimensions */
		for (int i=1; i<3; ++i){
			resCalc = new UniformResolution(); 
			resCalc.setExtremes(0.0, Math.PI / 2);
			resCalc.setResolution(1.0);
			shp.setDimensionResolution(dims[i], resCalc);
		}
		/*
		 * Try first with solid boundaries.
		 */
		for ( DimName d : dims )
			for ( int extreme = 0; extreme < 2; extreme++ )
				shp.setBoundary(d, extreme, new SolidBoundary(d, extreme));

		/* Check it is correct. */
		Log.out(DEBUG, "Solid boundaries");
		/* sphere with polar length pi / 2 and res 1 has 20 voxels in total */
		checkIterationSamples(shp, coords, trueNhb, trueArea, 20);
		Log.out(DEBUG, "");
		/*
		 * Now try with cyclic dimensions.
		 */
		shp.makeCyclic(dims[2]); /* only theta can be cyclic in the sphere */
		trueNhb = new int[][][]{
			/* current sample (0,0,0) */
			{ {0,0,0}, {0,0,0}, {1,0,0}, {1,1,0}, {1,1,1}, {1,2,0}, {1,2,1} }, 
			/* current sample (1,0,0) */
			{ {0,0,0}, {1,0,0}, {1,0,0}, {1,1,0}, {1,1,1}, {2,0,0}, {2,1,0},
			  {2,1,1} }, 	
			/* current sample (1,1,0) */
			{ {0,0,0}, {1,0,0}, {1,1,1}, {1,1,1}, {1,2,0}, {2,1,0}, {2,2,0},
			  {2,2,1}, {2,3,0}, {2,3,1} },
			/* current sample (2,2,1) */
			{ {1,1,0}, {1,1,1}, {2,1,0}, {2,1,1}, {2,2,0}, {2,2,2}, {2,3,1},
			  {2,3,2} }, 		
		};
		Log.out(DEBUG, "Cyclic dimensions");
		/* sphere with polar length pi / 2 and res 1 has 20 voxels in total */
		checkIterationSamples(shp, coords, trueNhb, 20);
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
				if ( shp.isNbhIteratorInside() )
					nhbCount++;
			}
			coord = shp.iteratorCurrent();
			Log.out(DEBUG, "Coord "+Vector.toString(coord)+" has "+nhbCount+" neighbors");
			assertEquals(nhbCount, trueNhb[coord[0]][coord[1]]);
		}
		assertEquals(iterCount, 9);
	}
	
	/**
	 * Evaluates the neighbor iterator at coordinates <b>sample_coords</b>.
	 * Assures that exactly the neighbors defined in <b>trueNhb</b> are visited
	 * for each sample coordinate.
	 * Assures the total number of voxels in the shape is equal to 
	 * <b>nVoxelTotal</b>.
	 * 
	 * @param shp A Shape.
	 * @param sample_coords Several 3D sample coordinates to be evaluated.
	 * @param trueNhb Array of true neighbors of the sample coordinates.  
	 * @param nVoxelTotal The number of voxels in <b>shp</b> in total.
	 */
	private void checkIterationSamples(Shape shp, int[][] sample_coords,
			int[][][] trueNhb, int nVoxelTotal)
	{
		checkIterationSamples(shp, sample_coords, trueNhb, null, nVoxelTotal);
	}
	
	/**
	 * Evaluates the neighbor iterator at coordinates <b>sample_coords</b>.
	 * Assures that exactly the neighbors defined in <b>trueNhb</b> are visited
	 * for each sample coordinate.
	 * Assures the total number of voxels in the shape is equal to 
	 * <b>nVoxelTotal</b>.
	 * Assures that the shared surface area between the neighbors and the
	 * current coord are equal to <b>trueAreas</b>, if not null.
	 * 
	 * @param shp A Shape.
	 * @param sample_coords Several 3D sample coordinates to be evaluated.
	 * @param trueNhb Array of true neighbors of the sample coordinates.  
	 * @param nVoxelTotal The number of voxels in <b>shp</b> in total.
	 */
	private void checkIterationSamples(Shape shp, int[][] sample_coords,
			int[][][] trueNhb, double[][] trueAreas, int nVoxelTotal)
	{
		int[] coord, nhb;
		int iter_count = 0, sample_count = 0, nhb_count = 0;
		for ( coord = shp.resetIterator(); shp.isIteratorValid(); 
									iter_count++, coord = shp.iteratorNext() )
		{
			/* only evaluate sample coordinates */
			if (sample_count < sample_coords.length 
					&& Vector.areSame(sample_coords[sample_count], coord)){
				nhb_count = 0;
				/* iterate through all neighbors */
				for ( nhb = shp.resetNbhIterator();
						shp.isNbhIteratorValid(); nhb = shp.nbhIteratorNext() )
				{
					if ( shp.isNbhIteratorInside() ){
						/* check equality of neighbor and trueNhb coords */
						Log.out(DEBUG, "Comparing current nhb " 
							+ Vector.toString(nhb) + " with true nhb "
							+ Vector.toString(trueNhb[sample_count][nhb_count]));
						assertTrue(Vector.areSame(
								trueNhb[sample_count][nhb_count], nhb));
						if (trueAreas != null){
							Log.out(DEBUG, "Comparing current nhb area " 
									+ shp.nhbCurrSharedArea() 
									+ " with true nhb area "
									+ trueAreas[sample_count][nhb_count]);
							assertTrue(ExtraMath.areEqual(
									shp.nhbCurrSharedArea(), 
									trueAreas[sample_count][nhb_count],
									1e-5));
						}
						nhb_count++;
					}
					
				}
				
				Log.out(DEBUG, "Coord " + Vector.toString(coord) + " has "
						+ trueNhb[sample_count].length
						+ " true neighbors, counted " + nhb_count);
				/* check all neighbors have been visited for the sample coord */
				assertEquals(nhb_count, trueNhb[sample_count].length);
				sample_count++;
			}
		}
		/* check all voxels have been visited */
		Log.out(DEBUG, "Shape has "	+ nVoxelTotal
				+ " voxels, counted " + iter_count);
		assertEquals(iter_count, nVoxelTotal);
	}
	
	@Test
	public void redBlackIteratorShouldIterateCorrectly()
	{
		AllTests.setupSimulatorForTest(1.0, 1.0,
				"redBlackIteratorShouldIterateCorrectly");
		CartesianShape shp = new Rectangle();
		UniformResolution resCalc = new UniformResolution();
		resCalc.setExtremes(0.0, 4.0);
		resCalc.setResolution(1.0);
		DimName[] dims = new DimName[]{DimName.X, DimName.Y};
		for ( DimName d : dims )
			shp.setDimensionResolution(d, resCalc);
		/*
		 * Try first with solid boundaries.
		 */
		for ( DimName d : dims )
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				SolidBoundary bndry = new SolidBoundary(d, extreme);
				shp.setBoundary(d, extreme, bndry);
			}
		Log.out(DEBUG, "Solid boundaries");
		checkRedBlackIteration(shp);
		Log.out(DEBUG, "");
		/*
		 * Now try with cyclic dimensions.
		 */
		for ( DimName d : dims )
			shp.makeCyclic(d);
		Log.out(DEBUG, "Cyclic dimensions");
		checkRedBlackIteration(shp);
		Log.out(DEBUG, "");	
	}
	
	private void checkRedBlackIteration(CartesianShape shape)
	{
		/* Reset the iterator. */
		shape.setNewIterator(2);
		int[] coord = shape.resetIterator();
		int[] oldCoord = coord.clone();
		coord = shape.iteratorNext();
		int[] nhb;
		/* Check that no coordinate has the previous one as its neighbour. */
		while ( shape.isIteratorValid() )
		{
			Log.out(DEBUG, "coord: "+Vector.toString(coord)+
					", oldCoord: "+Vector.toString(oldCoord));
			for ( nhb = shape.resetNbhIterator(); 
					shape.isNbhIteratorValid();
					nhb = shape.nbhIteratorNext())
			{
				Log.out(DEBUG, "   nhb: "+Vector.toString(nhb));
				assertFalse(Vector.areSame(nhb, oldCoord));
			}
			Vector.copyTo(oldCoord, coord);
			coord = shape.iteratorNext();
		}
	}
}