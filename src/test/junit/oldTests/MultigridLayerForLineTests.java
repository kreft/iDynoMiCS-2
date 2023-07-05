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
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import solver.multigrid.MultigridLayer;
import test.OldTests;

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
	private final static double wellMixed = 0.5;
	
	@Before
	public void setup()
	{
		Shape shape = OldTests.GetShape("Line");
		Dimension x = shape.getDimension(DimName.X);
		x.setLength(8.0);
		ResolutionCalculator resCalc = new MultigridResolution(x);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		shape.makeCyclic(DimName.X);
		SpatialGrid grid = new SpatialGrid(shape, "grid", null);
		/* Use a constant value in the concentration array. */
		grid.newArray(ArrayType.CONCN, concn);
		/* Use a variable value in the production array. */
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
	public void coarsestGridHasVariableValuesCopiedFromFiner()
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
	
	@Test
	public void finerGridHasConstantValuesCopiedFromCoarser()
	{
		/* Arrange */
		this._coarser.getGrid().newArray(ArrayType.WELLMIXED, wellMixed);
		/* Act */
		this._finer.getGrid().newArray(ArrayType.WELLMIXED, wellMixed - 1.0);
		this._finer.fillArrayFromCoarser(ArrayType.WELLMIXED, null);
		/* Assert */
		SpatialGrid grid = this._finer.getGrid();
		Shape shape = grid.getShape();
		for ( shape.resetIterator();
				shape.isIteratorValid(); shape.iteratorNext())
		{
			assertEquals(wellMixed,
					grid.getValueAtCurrent(ArrayType.WELLMIXED), TOLERANCE);
		}
	}
	
	@Test
	public void finestGridHasConstantValuesInterpolatedFromCoarser()
	{
		/* Arrange */
		this._coarser.getGrid().newArray(ArrayType.DIFFUSIVITY);
		double[][][] diffusivity = Array.zerosDbl(4, 1, 1);
		for ( int i = 0; i < 4; i++ )
			diffusivity[i][0][0] = 1.0 + (i%2==0 ? 0.0 : 2.0);
		this._coarser.getGrid().setTo(ArrayType.DIFFUSIVITY, diffusivity);
		/* Act */
		this._finer.getGrid().newArray(ArrayType.DIFFUSIVITY);
		this._finer.fillArrayFromCoarser(ArrayType.DIFFUSIVITY, null);
		/* Assert */
		SpatialGrid grid = this._finer.getGrid();
		Shape shape = grid.getShape();
		int[] current;
		double trueValue;
		for ( current = shape.resetIterator();
				shape.isIteratorValid(); current = shape.iteratorNext())
		{
			trueValue = 1.0 + (current[0]%2);
			if (current[0]%2 == 0)
				trueValue += current[0]%4;
			assertEquals(trueValue,
					grid.getValueAtCurrent(ArrayType.DIFFUSIVITY), TOLERANCE);
		}
	}
}
