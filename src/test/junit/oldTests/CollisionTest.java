package test.junit.oldTests;

import org.junit.Test;

import compartment.Compartment;
import dataIO.Log;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import surface.Rod;
import surface.Voxel;

/**
 * \brief: Unit tests for surface intersection.
 * 
 * @author Bastiaan
 *
 */
public class CollisionTest {
	
	@Test
	public void test2D()
	{
		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
		Compartment com = Idynomics.simulator.getCompartment("5x5x5");

		Log.out("2D test");
		/* tip to tip hit */
		tiptest(com, 0.6, 1.1, 0.4, 0.0, 0.5, 2);
		/* flipped tip to tip hit */
		tiptest(com, 0.0, 0.5, 0.4, 0.6, 1.1, 2);
		/* tip to tip miss */
		tiptest(com, 0.9, 1.4, 0.4, 0.0, 0.5, 2);
	}
	
	@Test
	public void test3D()
	{
		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
		Compartment com = Idynomics.simulator.getCompartment("5x5x5");

		Log.out("3D test");
		/* tip to tip hit */
		tiptest(com, 0.6, 1.1, 0.4, 0.0, 0.5, 3);
		/* flipped tip to tip hit */
		tiptest(com, 0.0, 0.5, 0.4, 0.6, 1.1, 3);
		/* tip to tip miss */
		tiptest(com, 0.9, 1.4, 0.4, 0.0, 0.5, 3);
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
		System.out.println( out );
		return out;
	}
}
