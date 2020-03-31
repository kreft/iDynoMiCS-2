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
 * \brief Set of tests for the PDEmultigrid solver class, that focuses on Line
 * shapes.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEmultigridTestsForLine
{
//	/* ***********************************************************************
//	 * SETUP
//	 * **********************************************************************/
//	
//	private double _numVoxels = Math.pow(2.0, 4.0);
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
//		this._shape = OldTests.GetShape("Line");
//		Dimension x = this._shape.getDimension(DimName.X);
//		x.setLength(this._numVoxels);
//		ResolutionCalculator resCalc = new MultigridResolution(x);
//		resCalc.setResolution(1.0);
//		this._shape.setDimensionResolution(DimName.X, resCalc);
//		
//		/* Set up the grids */
//		this._solute1 = this.getSoluteGrid("solute1");
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
//	public void multigridPdeConvergesForSimpleLine()
//	{
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* The PDE multigrid solver. */
//		this._solver.init(new String[] { "solute1" }, false);
//		this._solver.setUpdater(new PDEupdater() { } );
//		/* Solve the diffusion. */
//		this._solver.solve(OldTests.gridsAsCollection(this._solute1),
//				this._common, 1.0);
//		/* Confirm that diffusion has smoothed out the concentration. */
//		this.assertConvergence(this._solute1);
//	}
//	
//	@Test
//	public void multigridPdeConvergesForSimpleCyclicLine()
//	{
//		this._shape.makeCyclic(DimName.X);
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* The PDE multigrid solver. */
//		this._solver.init(new String[] { "solute1" }, false);
//		this._solver.setUpdater(new PDEupdater() { } );
//		/* Solve the diffusion. */
//		this._solver.solve(OldTests.gridsAsCollection(this._solute1),
//				this._common, 1.0);
//		/* Confirm that diffusion has smoothed out the concentration. */
//		this.assertConvergence(this._solute1);
//	}
//	
//	@Test
//	public void multigridPdeConvergesForSimpleLineWithWellMixed()
//	{
//		/* Set up a concentration gradient to be smoothed out. */
//		this.setUnevenConcn(this._solute1);
//		/* */
//		int numVMO = (int)(this._numVoxels) - 1;
//		int[] coord = Vector.zerosInt(3);
//		this._common.setValueAt(WELLMIXED, coord, 1.0);
//		this._solute1.setValueAt(CONCN, coord, numVMO);
//		coord[0] = numVMO;
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
//		for ( int i = 0; i < this._numVoxels; i++ )
//			assertEquals(this._numVoxels - i - 1.0, concn[i][0][0], TOLERANCE_SOFT);
//	}
//	
//	/* ***********************************************************************
//	 * HELPERS
//	 * **********************************************************************/
//	
//	private SpatialGrid getSoluteGrid(String name)
//	{
//		SpatialGrid grid = new SpatialGrid(this._shape, name, null);
//		grid.newArray(CONCN);
//		grid.newArray(DIFFUSIVITY, 1.0);
//		grid.newArray(PRODUCTIONRATE, 0.0);
//		return grid;
//	}
//	
//	private void setUnevenConcn(SpatialGrid grid)
//	{
//		double[][][] concn = grid.getArray(CONCN);
//		for ( int i = 0; i < this._numVoxels; i++ )
//			concn[i][0][0] = i;
//		grid.setTo(CONCN, concn);
//	}
//
//	private void assertConvergence(SpatialGrid grid)
//	{
//		double[][][] concn = grid.getArray(CONCN);
//		assertTrue(Double.isFinite(concn[0][0][0]));
//		for ( int i = 1; i < this._numVoxels; i++ )
//		{
//			assertEquals(concn[0][0][0], concn[i][0][0], TOLERANCE_SOFT);
//		}
//	}
}
