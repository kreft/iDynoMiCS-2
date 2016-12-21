package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static test.AllTests.TOLERANCE;

import org.junit.Test;

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
		ResolutionCalculator resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, 4.0);
		assertEquals(4, resCalc.getNVoxel());
		assertEquals(1.0, resCalc.getResolution(), TOLERANCE);
	}
	
	@Test
	public void getsCoarserMultigridResolutionWhenShould()
	{
		MultigridResolution resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, 4.0);
		ResolutionCalculator coarser = resCalc.getCoarserResolution();
		assertTrue(coarser instanceof MultigridResolution);
		assertEquals(2, coarser.getNVoxel());
		assertEquals(2.0, coarser.getResolution(), TOLERANCE);
	}
	
	@Test
	public void getsSingleVoxelWhenShould()
	{
		MultigridResolution resCalc = new MultigridResolution();
		resCalc.init(1.0, 0.0, 2.0);
		ResolutionCalculator coarser = resCalc.getCoarserResolution();
		assertTrue(coarser instanceof SingleVoxel);
		assertEquals(1, coarser.getNVoxel());
		assertEquals(2.0, coarser.getResolution(), TOLERANCE);
	}
}
