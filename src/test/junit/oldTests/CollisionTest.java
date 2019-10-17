package test.junit.oldTests;

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
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class CollisionTest implements Testable {
	
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

		Tester.println("2D test", mode);
		/* tip to tip hit */
		Tester.assess( tiptest(com, 0.6, 1.1, 0.4, 0.0, 0.5, 2), true, mode);
		/* flipped tip to tip hit */
		Tester.assess( tiptest(com, 0.0, 0.5, 0.4, 0.6, 1.1, 2), true, mode);
		/* tip to tip miss */
		Tester.assess( tiptest(com, 0.9, 1.4, 0.4, 0.0, 0.5, 2), false, mode);

		com = Idynomics.simulator.getCompartment("5x5_periodic");

		Tester.println("2D test periodic", mode);
		/* tip to tip hit */
		Tester.assess( tiptest(com, 4.3, 4.9, 0.4, 0.0, 0.5, 2), true, mode);
		/* tip to tip miss */
		Tester.assess( tiptest(com, 3.9, 4.5, 0.4, 0.0, 0.5, 2), false, mode);
		/* pass trough hit */
		Tester.assess( tiptest(com, 4.6, 0.2, 0.4, 0.5, 1.0, 2), true, mode);
		com = Idynomics.simulator.getCompartment("5x5x5");

		Tester.println("3D test", mode);
		/* tip to tip hit */
		Tester.assess( tiptest(com, 0.6, 1.1, 0.4, 0.0, 0.5, 3), true, mode);
		/* flipped tip to tip hit */
		Tester.assess( tiptest(com, 0.0, 0.5, 0.4, 0.6, 1.1, 3), true, mode);
		/* tip to tip miss */
		Tester.assess( tiptest(com, 0.9, 1.4, 0.4, 0.0, 0.5, 3), false, mode);
	}

	private boolean tiptest(Compartment com, double lr, double hr, double rr, 
			double lv, double hv, int dim)
	{
		return tiptest(com, Vector.vector(dim,lr), Vector.vector(dim,hr), rr,
				Vector.vector(dim,lv), Vector.vector(dim,hv) );
	}
	
	private boolean tiptest(Compartment com, double[] lr, double hr[], double rr, 
			double lv[], double hv[])
	{

		Rod rod = new Rod( lr, hr, rr );
		rod.init(com.getShape().getCollision());

		Voxel vox = new Voxel( lv, hv );
		vox.init(com.getShape().getCollision());

		boolean out = rod.collisionWith(vox);
		return out;
	}
}
