package test.junit.oldTests;

import org.junit.Test;

import compartment.Compartment;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import surface.Rod;
import surface.Voxel;

public class CollisionTest {

//	@Test
//	public void periodicTest2D()
//	{
//		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
//		Compartment com = Idynomics.simulator.getCompartment("5x5");
//
//		
//		Rod rod = new Rod(
//				new double[] {0.7, 0.7}, 	// second point
//				new double[] {1.1, 1.1}, 	// first point
//				0.5);						// radius
//		rod.init(com.getShape().getCollision());
//
//		Voxel vox = new Voxel(
//				new double[] {0.0, 0.0},	// lower corner
//				new double[] {0.5, 0.5});	// upper corner
//		vox.init(com.getShape().getCollision());
//
//		System.out.println( rod.collisionWith(vox) );
//	}
	
	@Test
	public void test3D()
	{
		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
		Compartment com = Idynomics.simulator.getCompartment("5x5x5");

		/* tip to tip hit */
		tiptest(com, 0.6, 1.1, 0.4, 0.0, 0.5);
		/* flipped tip to tip hit */
		tiptest(com, 0.0, 0.5, 0.4, 0.6, 1.1);
		/* tip to tip miss */
		tiptest(com, 0.9, 1.4, 0.4, 0.0, 0.5);
	}
	
	private boolean tiptest(Compartment com, double lr, double hr, double rr, 
			double lv, double hv)
	{
		return tiptest(com, Vector.vector(3,lr), Vector.vector(3,hr), rr,
				Vector.vector(3,lv), Vector.vector(3,hv) );
	}
	
	private boolean tiptest(Compartment com, double[] lr, double hr[], double rr, 
			double lv[], double hv[])
	{

		Rod rod = new Rod( lr, hr, rr );
		rod.init(com.getShape().getCollision());

		Voxel vox = new Voxel( lv, hv );
		vox.init(com.getShape().getCollision());

		boolean out = rod.collisionWith(vox);
		System.out.println( out );
		return out;
	}
}
