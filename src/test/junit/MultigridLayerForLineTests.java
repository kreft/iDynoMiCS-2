package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static test.AllTests.TOLERANCE;

import org.junit.Before;
import org.junit.Test;

import grid.ArrayType;
import grid.SpatialGrid;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import solver.multigrid.MultigridLayer;
import test.AllTests;

/**
 * \brief Set of tests for the MultigridLayer class, which is important for
 * the multigrid PDE solver, that focus on Line shapes.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class MultigridLayerForLineTests
{
	private MultigridLayer _finer, _coarser, _coarsest;
	
	private final static double concn = 1.47;
	
	@Before
	public void setup()
	{
		Shape shape = AllTests.GetShape("Line");
		ResolutionCalculator resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, 8.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		SpatialGrid grid = new SpatialGrid(shape, "grid", null);
		// Use a constant value in the concentration array
		grid.newArray(ArrayType.CONCN, concn);
		// Use a variable value in the production array
		grid.newArray(ArrayType.PRODUCTIONRATE);
		double[][][] prodRate = Array.zerosDbl(8, 1, 1);
		for ( int i = 0; i < 8; i++ )
			prodRate[i][0][0] = i;
		grid.setTo(ArrayType.PRODUCTIONRATE, prodRate);
		this._finer = new MultigridLayer(grid);
		this._coarser = this._finer.constructCoarser();
		this._coarsest = this._coarser.constructCoarser();
	}
	
	@Test
	public void coarserLayerHasHalfAsManyVoxels()
	{
		SpatialGrid grid = this._coarser.getGrid();
		Shape shape = grid.getShape();
		ResolutionCalculator resCalc = 
				shape.getResolutionCalculator(Vector.zerosInt(3), 0);
		assertTrue(resCalc instanceof MultigridResolution);
		assertEquals(4, resCalc.getNVoxel());
		assertEquals(2.0, resCalc.getResolution(), TOLERANCE);
	}
	
	@Test
	public void coarsestLayerHasQuarterAsManyVoxels()
	{
		SpatialGrid grid = this._coarsest.getGrid();
		Shape shape = grid.getShape();
		ResolutionCalculator resCalc = 
				shape.getResolutionCalculator(Vector.zerosInt(3), 0);
		assertTrue(resCalc instanceof MultigridResolution);
		assertEquals(2, resCalc.getNVoxel());
		assertEquals(4.0, resCalc.getResolution(), TOLERANCE);
	}
	
	@Test
	public void coarsestLayerCannotBeMadeCoarser()
	{
		assertFalse(this._coarsest.hasCoarser());
	}
	
	@Test
	public void finerGridHasCorrectConcnValues()
	{
		// TODO this really ought to go into a GridTests class
		SpatialGrid grid = this._finer.getGrid();
		Shape shape = grid.getShape();
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			assertEquals(concn, 
					grid.getValueAtCurrent(ArrayType.CONCN), TOLERANCE);
		}
	}
	
	@Test
	public void coarserGridHasConstantValuesCopiedFromFiner()
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
	public void coarsestGridHasConcstantValuesCopiedFromFiner()
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
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			assertEquals((2 * shape.iteratorCurrent()[0]) + 0.5,
					grid.getValueAtCurrent(ArrayType.PRODUCTIONRATE), TOLERANCE);
		}
	}
	
	@Test
	public void coarsestGridHasVariableValuesInterpolatedFromFiner()
	{
		SpatialGrid grid = this._coarsest.getGrid();
		Shape shape = grid.getShape();
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			assertEquals((4 * shape.iteratorCurrent()[0]) + 1.5,
					grid.getValueAtCurrent(ArrayType.PRODUCTIONRATE), TOLERANCE);
		}
	}
}
