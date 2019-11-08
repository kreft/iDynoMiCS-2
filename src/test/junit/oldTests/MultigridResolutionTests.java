package test.junit.oldTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import org.junit.Test;

import shape.Dimension;
import shape.ShapeConventions.SingleVoxel;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;

/**
 * \brief Set of tests for the MultigridResolution class, which is important
 * to the multigrid PDE solver.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class MultigridResolutionTests
{
	@Test
	public void initChoosesCorrectNumberOfVoxels()
	{
		Dimension dimension = new Dimension();
		dimension.setLength(4.0);
		ResolutionCalculator resCalc = new MultigridResolution(dimension);
		resCalc.setResolution(1.0);
		assertEquals(4, resCalc.getNVoxel());
		assertEquals(1.0, resCalc.getResolution(), TOLERANCE);
	}
	
	@Test
	public void getsCoarserMultigridResolutionWhenShould()
	{
		Dimension dimension = new Dimension();
		dimension.setLength(4.0);
		MultigridResolution resCalc = new MultigridResolution(dimension);
		resCalc.setResolution(1.0);
		ResolutionCalculator coarser = resCalc.getCoarserResolution();
		assertTrue(coarser instanceof MultigridResolution);
		assertEquals(2, coarser.getNVoxel());
		assertEquals(2.0, coarser.getResolution(), TOLERANCE);
	}
	
	@Test
	public void getsSingleVoxelWhenShould()
	{
		Dimension dimension = new Dimension();
		dimension.setLength(2.0);
		MultigridResolution resCalc = new MultigridResolution(dimension);
		resCalc.setResolution(1.0);
		ResolutionCalculator coarser = resCalc.getCoarserResolution();
		assertTrue(coarser instanceof SingleVoxel);
		assertEquals(1, coarser.getNVoxel());
		assertEquals(2.0, coarser.getResolution(), TOLERANCE);
	}
}
