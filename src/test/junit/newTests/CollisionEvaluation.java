package test.junit.newTests;

import org.junit.Test;

import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Tester;
import debugTools.Testable;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import surface.Rod;
import surface.Voxel;

/**
 * \brief: Unit tests for surface intersection.
 * 
 * This test evaluates surface-surface intersection (collision) for scenarios with a known outcome.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class CollisionEvaluation implements Testable {
	
	@Test
	public void test()
	{
		test(TestMode.UNIT);
	}
	
	public void test(TestMode mode)
	{
		Log.set(Tier.CRITICAL);
		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
		Compartment com = Idynomics.simulator.getCompartment("5x5");

		Tester.println("2D voxel rod test", mode);
		/* tip hit */
		Tester.assess( rodVoxel(com, 0.6, 1.1, 0.4, 0.0, 0.5, 2), true, mode);
		/* flipped tip hit */
		Tester.assess( rodVoxel(com, 0.0, 0.5, 0.4, 0.6, 1.1, 2), true, mode);
		/* tip miss */
		Tester.assess( rodVoxel(com, 0.9, 1.4, 0.4, 0.0, 0.5, 2), false, mode);
		/* would hit in periodic, but not in non-periodic */
		Tester.assess( rodVoxel(com, 4.6, 0.2, 0.4, 0.4, 1.0, 2), false, mode);
		
		/* parallel hit */
		Tester.assess( rodVoxel(com, new double[] { 1.0, 0.0} , new double[] { 1.0, 1.0}, 0.4, 
				new double[] { 0.0, 0.0},  new double[] { 0.8, 0.8}), true, mode);

		/* parallel miss */
		Tester.assess( rodVoxel(com, new double[] { 1.3, 0.0} , new double[] { 1.3, 1.0}, 0.4, 
				new double[] { 0.0, 0.0},  new double[] { 0.8, 0.8}), false, mode);

		/* parallel hit repeat */
		Tester.assess( rodVoxel(com, new double[] { 1.0, 0.0} , new double[] { 1.0, 1.0}, 0.4, 
				new double[] { 0.0, 0.0},  new double[] { 0.8, 0.8}), true, mode);
		
		com = Idynomics.simulator.getCompartment("5x5_periodic");

		Tester.println("2D periodic voxel rod test ", mode);
		/* tip hit trough periodic boundary */
		Tester.assess( rodVoxel(com, 4.3, 4.9, 0.4, 0.0, 0.5, 2), true, mode);
		/* tip hit (rod shadow representation)*/
		Tester.assess( rodVoxel(com, -0.7, -0.1, 0.4, 0.0, 0.5, 2), true, mode);
		/* tip miss */
		Tester.assess( rodVoxel(com, 3.9, 4.5, 0.4, 0.0, 0.5, 2), false, mode);
		/* rod passes trough periodic boundary hit */
		Tester.assess( rodVoxel(com, 4.6, 0.2, 0.4, 0.4, 1.0, 2), true, mode);
		com = Idynomics.simulator.getCompartment("5x5x5");

		Tester.println("3D voxel rod test", mode);
		/* tip hit */
		Tester.assess( rodVoxel(com, 0.6, 1.1, 0.4, 0.0, 0.5, 3), true, mode);
		/* flipped tip hit */
		Tester.assess( rodVoxel(com, 0.0, 0.5, 0.4, 0.6, 1.1, 3), true, mode);
		/* tip miss */
		Tester.assess( rodVoxel(com, 0.9, 1.4, 0.4, 0.0, 0.5, 3), false, mode);
	}

	/**
	 * Quick method to setup rod voxel test with for every object x1 = y1 = z1,  x2 = y2 = z2, etc.
	 * 
	 * @param com spatial compartment
	 * @param lr first point location of rod
	 * @param hr second point location of rod
	 * @param rr rod radius
	 * @param lv lower voxel point
	 * @param hv higher voxel point
	 * @param dim number of dimensions
	 * @return true on surface-surface intersection, false if not
	 */
	private boolean rodVoxel(Compartment com, double lr, double hr, double rr, 
			double lv, double hv, int dim)
	{
		return rodVoxel(com, Vector.vector(dim,lr), Vector.vector(dim,hr), rr,
				Vector.vector(dim,lv), Vector.vector(dim,hv) );
	}
	
	/**
	 * Quick method to setup rod voxel test.
	 * 
	 * @param com spatial compartment
	 * @param lr first point location of rod
	 * @param hr second point location of rod
	 * @param rr rod radius
	 * @param lv lower voxel point
	 * @param hv higher voxel point
	 * @param dim number of dimensions
	 * @return true on surface-surface intersection, false if not
	 */
	private boolean rodVoxel(Compartment com, double[] lr, double hr[], double rr, 
			double lv[], double hv[])
	{

		Rod rod = new Rod( lr, hr, rr );
		rod.init(com.getShape().getCollision());

		Voxel vox = new Voxel( lv, hv );
		vox.init(com.getShape().getCollision());

		return rod.collisionWith(vox);
	}
}
