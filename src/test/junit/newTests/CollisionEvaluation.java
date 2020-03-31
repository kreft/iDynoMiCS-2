package test.junit.newTests;

import org.junit.Test;

import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Tester;
import debugTools.Testable;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import surface.*;
import surface.Rod;
import surface.Surface;
import surface.Voxel;

/**
 * \brief: Unit tests for surface intersection.
 * 
 * This test evaluates surface-surface intersection (collision) for scenarios 
 * with a known outcome, this test does NOT test surface to surface distance
 * calculation.
 * 
 * basic intersections that are tested for:
 *  sphere sphere
 *  rod sphere
 *  rod rod
 *  sphere plane
 *  rod plane
 *  voxel sphere (voxel are assumed to be never split be a periodic boundary)
 *  voxel rod
 *  
 * basic intersections that are NOT tested for:
 *  plane plane (planes are infinite so any non-parallel plane should intersect)
 *  voxel voxel (test is not implemented, but would be easy if required)
 *  voxel plane (test is not implemented, but would be easy if required)
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
		/* basic simulator initiation */
		Log.set(Tier.CRITICAL);
		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
		Compartment com = null;
		
		/* sphere sphere */
		Tester.println("2D periodic - sphere spehere", mode);
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 1.0,1.0 }, 0.5), 
				new Ball(new double[] { 1.5, 1.5 }, 0.5)), true, mode, 
				"basic hit");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 1.0,1.0 }, 0.3), 
				new Ball(new double[] { 1.5, 1.5 }, 0.3)), false, mode, 
				"basic miss");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 0.1,0.1 }, 0.5), 
				new Ball(new double[] { 4.6, 4.6 }, 0.5)), true, mode, 
				"periodic hit");		
		Tester.assess( surfaceEval(com, new Ball(new double[] { 0.1,0.1 }, 0.3), 
				new Ball(new double[] { 4.6, 4.6 }, 0.3)), false, mode, 
				"periodic miss");

		/* rod sphere */
		Tester.println("2D periodic - rod spehere", mode);
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.assess( surfaceEval(com, new Ball(new double[] {1.0,1.0}, 0.5), 
				new Rod(new double[] {1.5, 1.5}, new double[] {2.0,2.0}, 0.5)), 
				true, mode, "basic hit");
		Tester.assess( surfaceEval(com, new Ball(new double[] {1.0,1.0}, 0.3), 
				new Rod(new double[] {1.5, 1.5}, new double[] {2.0,2.0}, 0.3)), 
				false, mode, "basic miss");
		Tester.assess( surfaceEval(com, new Ball(new double[] {0.1,0.1}, 0.5), 
				new Rod(new double[] {4.1, 4.1}, new double[] {4.6,4.6}, 0.5)), 
				true, mode, "periodic hit");		
		Tester.assess( surfaceEval(com, new Ball(new double[] {0.1,0.1}, 0.3), 
				new Rod(new double[] {4.1, 4.1}, new double[] {4.6,4.6}, 0.3)), 
				false, mode, "periodic miss");
		
		/* rod rod */
		Tester.println("2D periodic - rod rod ", mode);
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.assess( surfaceEval(com, 
				new Rod(new double[] {0.5, 0.5}, new double[] {1.0,1.0}, 0.5), 
				new Rod(new double[] {1.5, 1.5}, new double[] {2.0,2.0}, 0.5)), 
				true, mode, "basic hit");
		Tester.assess( surfaceEval(com,
				new Rod(new double[] {0.5, 0.5}, new double[] {1.0,1.0}, 0.3), 
				new Rod(new double[] {1.5, 1.5}, new double[] {2.0,2.0}, 0.3)), 
				false, mode, "basic miss");
		Tester.assess( surfaceEval(com,
				new Rod(new double[] {0.1, 0.1}, new double[] {1.0,1.0}, 0.5), 
				new Rod(new double[] {4.1, 4.1}, new double[] {4.6,4.6}, 0.5)), 
				true, mode, "periodic hit");		
		Tester.assess( surfaceEval(com, 
				new Rod(new double[] {0.1, 0.1}, new double[] {1.0,1.0}, 0.3), 
				new Rod(new double[] {4.1, 4.1}, new double[] {4.6,4.6}, 0.3)), 
				false, mode, "periodic miss");
		
		/* plane sphere */
		Tester.println("2D periodic - plane sphere", mode);
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 0.4,3.0 }, 0.5),
				new Plane(new double[] { 1.0, 0.0 }, new double[] {0.0,0.0} )), 
				true, mode, "basic hit");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 0.6,0.1 }, 0.5),
				new Plane(new double[] { 1.0, 0.0 }, new double[] {0.0,0.0 })), 
				false, mode, "basic miss");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 4.7,3.0 }, 0.5),
				new Plane(new double[] { -1.0, 0.0 }, new double[] {0.0,0.0} )), 
				true, mode, "periodic hit !NOTE: planes only evaluate in the "
				+ "direction of their normal,\n\t for a two sided evaluation "
				+ "create an additional plane with -normal.");
		
		/* plane rod */
		Tester.println("2D periodic - plane rod", mode);
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.assess( surfaceEval(com,
				new Rod(new double[] {0.4, 3.5}, new double[] {1.4, 3.5}, 0.5), 
				new Plane(new double[] {1.0, 0.0}, new double[] {0.0, 0.0} )), 
				true, mode, "basic hit");
		Tester.assess( surfaceEval(com,
				new Rod(new double[] {3.5, 0.4}, new double[] {3.5, 1.4}, 0.5), 
				new Plane(new double[] {1.0, 0.0}, new double[] {0.0, 0.0} )), 
				false, mode, "basic miss");
		Tester.assess( surfaceEval(com, 
				new Rod(new double[] {4.6, 0.4}, new double[] {4.5, 1.4}, 0.5),
				new Plane(new double[] { -1.0, 0.0 }, new double[] {0.0,0.0} )), 
				true, mode, "periodic hit");
		
		/* voxel sphere */
		Tester.println("2D periodic - voxel sphere", mode);
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 1.0,1.0 }, 0.5), 
				new Voxel( new double[] {0.0, 0.0}, new double[] {0.8, 0.8})), 
				true, mode, "basic hit");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 1.3,1.3 }, 0.5), 
				new Voxel( new double[] {0.0, 0.0}, new double[] {0.8, 0.8})), 
				false, mode, "basic miss");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 4.7,4.7 }, 0.5), 
				new Voxel( new double[] {0.0, 0.0}, new double[] {0.8, 0.8})), 
				true, mode, "periodic hit");
		Tester.assess( surfaceEval(com, new Ball(new double[] { 4.7,4.7 }, 0.3), 
				new Voxel( new double[] {0.0, 0.0}, new double[] {0.8, 0.8})), 
				false, mode, "periodic miss");
		
		/* voxel rod */
		com = Idynomics.simulator.getCompartment("5x5");
		Tester.println("2D - voxel rod (not periodic)", mode);
		/* tip hit */
		Tester.assess( rodVoxel(com, 0.6, 1.1, 0.4, 0.0, 0.5, 2), true, mode, 
				"basic hit");
		/* flipped tip hit */
		Tester.assess( rodVoxel(com, 0.0, 0.5, 0.4, 0.6, 1.1, 2), true, mode, 
				"basic hit opposite side");
		/* tip miss */
		Tester.assess( rodVoxel(com, 0.9, 1.4, 0.4, 0.0, 0.5, 2), false, mode, 
				"basic miss");
		/* would hit in periodic, but not in non-periodic */
		Tester.assess( rodVoxel(com, 4.6, 0.2, 0.4, 0.4, 1.0, 2), false, mode, 
				"would hit in periodic");
		
		/* parallel hit */
		Tester.assess( rodVoxel(com, new double[] { 1.0, 0.0} , new double[] 
				{ 1.0, 1.0}, 0.4, new double[] { 0.0, 0.0},  new double[] 
				{ 0.8, 0.8}), true, mode, "parallel hit");
		/* parallel miss */
		Tester.assess( rodVoxel(com, new double[] { 1.3, 0.0} , new double[] 
				{ 1.3, 1.0}, 0.4, new double[] { 0.0, 0.0},  new double[] 
				{ 0.8, 0.8}), false, mode, "parallel miss");
		/* parallel hit repeat 
		 * NOTE: this test failed before due to out dated collision variables*/
		Tester.assess( rodVoxel(com, new double[] { 1.0, 0.0} , new double[] 
				{ 1.0, 1.0}, 0.4, new double[] { 0.0, 0.0},  new double[] 
				{ 0.8, 0.8}), true, mode, "repeated parallel hit");
		
		com = Idynomics.simulator.getCompartment("5x5_periodic");
		Tester.println("2D periodic - voxel rod", mode);
		/* tip hit trough periodic boundary */
		Tester.assess( rodVoxel(com, 4.3, 4.9, 0.4, 0.0, 0.5, 2), true, mode, 
				"hit trough periodic boundry, rod rad only");
		/* tip hit (rod shadow representation)*/
		Tester.assess( rodVoxel(com, -0.7, -0.1, 0.4, 0.0, 0.5, 2), true, mode, 
				"hit with shadow representation");
		/* tip miss */
		Tester.assess( rodVoxel(com, 3.9, 4.5, 0.4, 0.0, 0.5, 2), false, mode, 
				"periodic miss");
		/* rod passes trough periodic boundary hit */
		Tester.assess( rodVoxel(com, 4.6, 0.2, 0.4, 0.4, 1.0, 2), true, mode, 
				"periodic hit, rod mass points on opposite sides");

		com = Idynomics.simulator.getCompartment("5x5x5");
		Tester.println("3D - voxel rod", mode);
		/* tip hit */
		Tester.assess( rodVoxel(com, 0.6, 1.1, 0.4, 0.0, 0.5, 3), true, mode, 
				"basic hit");
		/* flipped tip hit */
		Tester.assess( rodVoxel(com, 0.0, 0.5, 0.4, 0.6, 1.1, 3), true, mode, 
				"basic hit opposite side");
		/* tip miss */
		Tester.assess( rodVoxel(com, 0.9, 1.4, 0.4, 0.0, 0.5, 3), false, mode, 
				"basic miss");
	}
	
	/* helper methods *********************************************************/

	/**
	 * Evaluate whether two input surfaces are intersecting
	 * 
	 * @param com - compartment for evaluation (this can be any shape, periodic
	 * etc.
	 * @param first - first Surface object
	 * @param second - second Surface object
	 * @return true on collision, false on miss
	 */
	private boolean surfaceEval(Compartment com, Surface first, Surface second)
	{
		first.init(com.getShape().getCollision());
		second.init(com.getShape().getCollision());
		return first.collisionWith(second);
	}
	/**
	 * Quick method to setup rod voxel test with for every object x1 = y1 = z1,
	 * x2 = y2 = z2, etc.
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
	private boolean rodVoxel(Compartment com, double[] lr, double hr[], 
			double rr, 	double lv[], double hv[])
	{
		Rod rod = new Rod( lr, hr, rr );
		Voxel vox = new Voxel( lv, hv );
		return surfaceEval(com, rod, vox);
	}
}
