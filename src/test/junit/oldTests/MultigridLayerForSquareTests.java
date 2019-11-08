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
 * the multigrid PDE solver, that focus on square Rectangle shapes.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class MultigridLayerForSquareTests
{
	private MultigridLayer _finer, _coarser, _coarsest;
	
	private final static double concn = 1.47;
	private final static double wellMixed = 0.5;
	
	@Before
	public void setup()
	{
		/*
		 * Set up the shape.
		 */
		Shape shape = OldTests.GetShape("Rectangle");
		/* X dimension */
		Dimension x = shape.getDimension(DimName.X);
		x.setLength(8.0);
		ResolutionCalculator resCalc = new MultigridResolution(x);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.X, resCalc);
		shape.makeCyclic(DimName.X);
		/* Y dimension */
		Dimension y = shape.getDimension(DimName.X);
		y.setLength(8.0);
		resCalc = new MultigridResolution(y);
		resCalc.setResolution(1.0);
		shape.setDimensionResolution(DimName.Y, resCalc);
		shape.makeCyclic(DimName.Y);
		/*
		 * Set up the grid.
		 */
		SpatialGrid grid = new SpatialGrid(shape, "grid", null);
		/* Use a constant value in the concentration array. */
		grid.newArray(ArrayType.CONCN, concn);
		/* Use a variable value in the production array. */
		grid.newArray(ArrayType.PRODUCTIONRATE);
		double[][][] prodRate = Array.zerosDbl(8, 8, 1);
		for ( int i = 0; i < 8; i++ )
			for (int j = 0; j < 8; j++ )
				prodRate[i][j][0] = (i*8) + j;
		grid.setTo(ArrayType.PRODUCTIONRATE, prodRate);
		/*
		 * Set up the multilayer.
		 */
		this._finer = new MultigridLayer(grid);
		this._coarser = this._finer.constructCoarser();
		this._coarsest = this._coarser.constructCoarser();
	}
	
	@Test
	public void coarserLayerHasHalfAsManyVoxelsInEachDimension()
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
		assertEquals(4, resCalc.getNVoxel());
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
		this._coarsest.getGrid().newArray(ArrayType.DIFFUSIVITY);
		double[][][] diffusivity = Array.zerosDbl(2, 2, 1);
		for ( int i = 0; i < 2; i++ )
			for ( int j = 0; j < 2; j++ )
				diffusivity[i][j][0] = 1.0 + (i+j)%2;
		this._coarsest.getGrid().setTo(ArrayType.DIFFUSIVITY, diffusivity);
		/* Act */
		this._coarser.getGrid().newArray(ArrayType.DIFFUSIVITY);
		this._coarser.fillArrayFromCoarser(ArrayType.DIFFUSIVITY, null);
		/* Assert */
		SpatialGrid grid = this._coarser.getGrid();
		Shape shape = grid.getShape();
		int[] current;
		double trueValue;
		for ( current = shape.resetIterator();
				shape.isIteratorValid(); current = shape.iteratorNext())
		{
			trueValue = 1.0;
			if ( current[0] != current[1] )
				trueValue += 0.5 * (1 + ((current[0]+current[1] + 1)%2));
			assertEquals(trueValue,
					grid.getValueAtCurrent(ArrayType.DIFFUSIVITY), TOLERANCE);
		}
	}
}
