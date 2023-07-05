package test.junit.oldTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import org.junit.Before;
import org.junit.Test;

import grid.ArrayType;
import grid.SpatialGrid;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.ShapeConventions.SingleVoxel;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import solver.multigrid.MultigridLayer;
import test.OldTests;

/**
 * \brief Set of tests for the MultigridLayer class, which is important for
 * the multigrid PDE solver, that focus on non-square Rectangle shapes.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class MultigridLayerForRectangleTests
{
	private MultigridLayer _finer, _coarser, _coarsest;
	
	private final static double concn = 1.47;
	
	@Before
	public void setup()
	{
		Shape shape = OldTests.GetShape("Rectangle");
		/* 8 voxels in x-dimension */
		Dimension x = shape.getDimension(DimName.X);
		x.setLength(8.0);
		ResolutionCalculator resCalc = new MultigridResolution(x);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		/* 4 voxels in y-dimension */
		Dimension y = shape.getDimension(DimName.X);
		y.setLength(4.0);
		resCalc = new MultigridResolution(y);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.Y, resCalc);
		SpatialGrid grid = new SpatialGrid(shape, "grid", null);
		/* Use a constant value in the concentration array. */
		grid.newArray(ArrayType.CONCN, concn);
		/* Use a variable value in the production array. */
		grid.newArray(ArrayType.PRODUCTIONRATE);
		double[][][] prodRate = Array.zerosDbl(8, 4, 1);
		for ( int i = 0; i < 8; i++ )
			for (int j = 0; j < 4; j++ )
				prodRate[i][j][0] = (i*8) + j;
		grid.setTo(ArrayType.PRODUCTIONRATE, prodRate);
		this._finer = new MultigridLayer(grid);
		this._coarser = this._finer.constructCoarser();
		this._coarsest = this._coarser.constructCoarser();
	}
	
	@Test
	public void coarserLayerHasHalfAsManyVoxelsIneachDimension()
	{
		SpatialGrid grid = this._coarser.getGrid();
		Shape shape = grid.getShape();
		ResolutionCalculator resCalc = 
				shape.getResolutionCalculator(Vector.zerosInt(3), 0);
		assertTrue(resCalc instanceof MultigridResolution);
		assertEquals(4, resCalc.getNVoxel());
		assertEquals(2.0, resCalc.getResolution(), TOLERANCE);
		resCalc = shape.getResolutionCalculator(Vector.zerosInt(3), 1);
		assertTrue(resCalc instanceof MultigridResolution);
		assertEquals(2, resCalc.getNVoxel());
		assertEquals(2.0, resCalc.getResolution(), TOLERANCE);
	}
	
	@Test
	public void coarsestLayerHasQuarterAsManyVoxelsInEachDimension()
	{
		SpatialGrid grid = this._coarsest.getGrid();
		Shape shape = grid.getShape();
		ResolutionCalculator resCalc = 
				shape.getResolutionCalculator(Vector.zerosInt(3), 0);
		assertTrue(resCalc instanceof MultigridResolution);
		assertEquals(2, resCalc.getNVoxel());
		assertEquals(4.0, resCalc.getResolution(), TOLERANCE);
		resCalc = shape.getResolutionCalculator(Vector.zerosInt(3), 1);
		assertTrue(resCalc instanceof SingleVoxel);
		assertEquals(1, resCalc.getNVoxel());
		assertEquals(4.0, resCalc.getResolution(), TOLERANCE);
	}
	
	@Test
	public void coarsestLayerCannotBeMadeCoarser()
	{
		assertFalse(this._coarsest.hasCoarser());
	}
	
	@Test
	public void coarserGridHasConcnValuesOfFiner()
	{
		SpatialGrid grid = this._coarser.getGrid();
		Shape shape = grid.getShape();
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			assertEquals(concn, 
					grid.getValueAtCurrent(ArrayType.CONCN), TOLERANCE);
		}
	}
	
	@Test
	public void coarsestGridHasConcnValuesOfFiner()
	{
		SpatialGrid grid = this._coarsest.getGrid();
		Shape shape = grid.getShape();
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			assertEquals(concn, 
					grid.getValueAtCurrent(ArrayType.CONCN), TOLERANCE);
		}
	}
	
	@Test
	public void coarserGridHasVariableValuesInterpolatedFromFiner()
	{
		SpatialGrid grid = this._coarser.getGrid();
		Shape shape = grid.getShape();
		double trueValue;
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			trueValue = (16 * shape.iteratorCurrent()[0]) +
					(2 * shape.iteratorCurrent()[1]) + 4.5;
			assertEquals(trueValue,
					grid.getValueAtCurrent(ArrayType.PRODUCTIONRATE), TOLERANCE);
		}
	}
	
	@Test
	public void coarsestGridHasVariableValuesInterpolatedFromFiner()
	{
		SpatialGrid grid = this._coarsest.getGrid();
		Shape shape = grid.getShape();
		double trueValue;
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			trueValue = (32 * shape.iteratorCurrent()[0]) +
					(4 * shape.iteratorCurrent()[1]) + 13.5;
			assertEquals(trueValue, grid.getValueAtCurrent(ArrayType.PRODUCTIONRATE), TOLERANCE);
		}
	}
}
