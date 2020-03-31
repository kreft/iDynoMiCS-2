package test.junit.oldTests;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.DIFFUSIVITY;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.WELLMIXED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE_SOFT;

import org.junit.Before;
import org.junit.Test;

import grid.SpatialGrid;
import linearAlgebra.Vector;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import solver.PDEmultigrid;
import solver.PDEupdater;
import test.OldTests;

/**
 * \brief Set of tests for the PDEmultigrid solver class, that focuses on 
 * square shapes (i.e. Rectangle shapes with equal X & Y dimensions).
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEmultigridTestsForSquare
{
//	/* ***********************************************************************
//	 * SETUP
//	 * **********************************************************************/
//	
//	private double _numVoxels = Math.pow(2.0, 3.0);
//	
//	private Shape _shape;
//	
//	private SpatialGrid _solute1;
//	
//	private SpatialGrid _common;
//	
//	private PDEmultigrid _solver;
//	
//	@Before
//	public void setup()
//	{
//		/* Set up the shape. */
//		this._shape = OldTests.GetShape("Rectangle");
//		for (DimName dimName : new DimName[] {DimName.X, DimName.Y})
//		{
//			Dimension dimension = this._shape.getDimension(dimName);
//			dimension.setLength(this._numVoxels);
//			ResolutionCalculator resCalc = new MultigridResolution(dimension);
//			resCalc.setResolution(1.0);
//			this._shape.setDimensionResolution(dimName, resCalc);
//		}
//		
//		/* Set up the grids */
//		this._solute1 = new SpatialGrid(this._shape, "solute", null);
//		this._solute1.newArray(CONCN);
//		this._solute1.newArray(DIFFUSIVITY, 1.0);
//		this._solute1.newArray(PRODUCTIONRATE, 0.0);
//		this._common = new SpatialGrid(this._shape, "common", null);
//		this._common.newArray(WELLMIXED, 0.0);
//		
//		this._solver = new PDEmultigrid();
//	}
//	
//	/* ***********************************************************************
//	 * TESTS
//	 * **********************************************************************/
//	
//	@Test
//	public void multigridPdeConvergesForSimpleSquare()
//	{
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* The PDE multigrid solver. */
//		this._solver.init(new String[] { "solute" }, false);
//		this._solver.setUpdater(new PDEupdater() { } );
//		/* Solve the diffusion. */
//		this._solver.solve(OldTests.gridsAsCollection(this._solute1),
//				this._common, 1.0);
//		/* Confirm that diffusion has smoothed out the concentration. */
//		this.assertConvergence(this._solute1);
//	}
//	
//	@Test
//	public void multigridPdeConvergesForSimpleOneCyclicSquare()
//	{
//		
//		this._shape.makeCyclic(DimName.X);
//		
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* The PDE multigrid solver. */
//		this._solver.init(new String[] { "solute" }, false);
//		this._solver.setUpdater(new PDEupdater() { } );
//		/* Solve the diffusion. */
//		this._solver.solve(OldTests.gridsAsCollection(this._solute1),
//				this._common, 1.0);
//		/* Confirm that diffusion has smoothed out the concentration. */
//		this.assertConvergence(this._solute1);
//	}
//	
//	@Test
//	public void multigridPdeConvergesForSimpleTwoCyclicSquare()
//	{
//		this._shape.makeCyclic(DimName.X);
//		this._shape.makeCyclic(DimName.Y);
//		
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* The PDE multigrid solver. */
//		this._solver.init(new String[] { "solute" }, false);
//		this._solver.setUpdater(new PDEupdater() { } );
//		/* Solve the diffusion. */
//		this._solver.solve(OldTests.gridsAsCollection(this._solute1),
//				this._common, 1.0);
//		/* Confirm that diffusion has smoothed out the concentration. */
//		this.assertConvergence(this._solute1);
//	}
//	
//	@Test
//	public void multigridPdeConvergesForSimpleSquareWithWellMixed()
//	{
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* */
//		int numVMO = (int)(this._numVoxels) - 1;
//		int[] coord = Vector.zerosInt(3);
//		this._common.setValueAt(WELLMIXED, coord, 1.0);
//		this._solute1.setValueAt(CONCN, coord, numVMO);
//		coord[0] = numVMO;
//		coord[1] = numVMO;
//		this._common.setValueAt(WELLMIXED, coord, 1.0);
//		this._solute1.setValueAt(CONCN, coord, 0.0);
//		/* The PDE multigrid solver. */
//		this._solver.init(new String[] { "solute1" }, false);
//		this._solver.setUpdater(new PDEupdater() { } );
//		/* Solve the diffusion. */
//		this._solver.solve(OldTests.gridsAsCollection(this._solute1),
//				this._common, 1.0);
//		/* Confirm that diffusion has reversed the concentration gradient. */
//		double[][][] concn = this._solute1.getArray(CONCN);
//		assertEquals(numVMO, concn[0][0][0], TOLERANCE_SOFT);
//		assertEquals(0.0, concn[numVMO][numVMO][0], TOLERANCE_SOFT);
//		/* Check that the matrix is symmetric. */
//		for ( int i = 0; i < this._numVoxels; i++ )
//			for ( int j = i + 1; j < this._numVoxels; j++ )
//				assertEquals(concn[j][i][0], concn[i][j][0], TOLERANCE_SOFT);
//		/*
//		 * Assert that the concentration decreases steadily as we go
//		 * from (0,0) to (numVMO, numVMO).
//		 */
//		for ( int i = 0; i < numVMO; i++ )
//			for ( int j = i; j < numVMO; j++ )
//			{
//				assertTrue(concn[i][j][0] > concn[i + 1][j][0]);
//				assertTrue(concn[i][j][0] > concn[i][j + 1][0]);
//				assertTrue(concn[i][j][0] > concn[i + 1][j + 1][0]);
//			}
//	}
//	
//	/* ***********************************************************************
//	 * HELPERS
//	 * **********************************************************************/
//
//
//	private void setUnevenConcn(SpatialGrid grid)
//	{
//		double[][][] concn = grid.getArray(CONCN);
//		for ( int i = 0; i < this._numVoxels; i++ )
//			for ( int j = 0; j < this._numVoxels; j++)
//				concn[i][j][0] = i + j;
//		grid.setTo(CONCN, concn);
//	}
//	
//	private void assertConvergence(SpatialGrid grid)
//	{
//		double[][][] concn = grid.getArray(CONCN);
//		assertTrue(Double.isFinite(concn[0][0][0]));
//		for ( int i = 1; i < this._numVoxels; i++ )
//			for ( int j = 0; j < this._numVoxels; j++ )
//				assertEquals(concn[0][0][0], concn[i][j][0], TOLERANCE_SOFT);
//	}
}
