package test.junit;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.DIFFUSIVITY;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.WELLMIXED;
import static test.AllTests.TOLERANCE;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import grid.SpatialGrid;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import solver.PDEmultigrid;
import solver.PDEupdater;
import test.AllTests;

/**
 * \brief 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEmultigridTestForLine
{
	@Test
	public void multigridPdeConvergesForSimpleLine()
	{
		double numVoxels = Math.pow(2.0, 3.0);
		
		/* Set up the shape. */
		Shape shape = AllTests.GetShape("Line");
		ResolutionCalculator resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, numVoxels);
		shape.setDimensionResolution(DimName.X, resCalc);
		//shape.makeCyclic(DimName.X);
		
		/* Set up the grids */
		SpatialGrid solute = new SpatialGrid(shape, "solute", null);
		solute.newArray(CONCN);
		solute.newArray(DIFFUSIVITY, 1.0);
		solute.newArray(PRODUCTIONRATE, 0.0);
		SpatialGrid common = new SpatialGrid(shape, "common", null);
		common.newArray(WELLMIXED, 0.0);
		/* Set up a concentration gradient to be smoothed out. */
		double[][][] concn = solute.getArray(CONCN);
		for ( int i = 0; i < numVoxels; i++ )
			concn[i][0][0] = i;
		solute.setTo(CONCN, concn);
		/* The PDE multigrid solver. */
		PDEmultigrid solver = new PDEmultigrid();
		solver.init(new String[] { "solute" }, false);
		solver.setUpdater(new PDEupdater() { } );
		/* Solve the diffusion. */
		Collection<SpatialGrid> grids = new LinkedList<SpatialGrid>();
		grids.add(solute);
		solver.solve(grids, common, 1.0);
		/* Confirm that diffusion has smoothed out the concentration. */
		concn = solute.getArray(CONCN);
		assertTrue(Double.isFinite(concn[0][0][0]));
		for ( int i = 1; i < numVoxels; i++ )
			assertEquals(concn[0][0][0], concn[i][0][0], TOLERANCE);
	}
}
