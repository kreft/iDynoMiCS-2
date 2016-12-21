package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static test.AllTests.TOLERANCE;

import org.junit.Before;
import org.junit.Test;

import grid.ArrayType;
import grid.SpatialGrid;
import linearAlgebra.Vector;
import shape.Shape;
import shape.ShapeConventions.SingleVoxel;
import shape.Dimension.DimName;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import solver.multigrid.MultigridLayer;
import test.AllTests;

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
		Shape shape = AllTests.GetShape("Rectangle");
		// 8 voxels in x-dimension
		ResolutionCalculator resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, 8.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		// 4 voxels in y-dimension
		resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, 4.0);
		shape.setDimensionResolution(DimName.Y, resCalc);
		SpatialGrid grid = new SpatialGrid(shape, "grid", null);
		grid.newArray(ArrayType.CONCN, concn);
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
}
