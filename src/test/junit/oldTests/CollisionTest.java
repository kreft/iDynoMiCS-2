package test.junit.oldTests;

import org.junit.Test;

import compartment.Compartment;
import idynomics.Idynomics;
import surface.Rod;
import surface.Voxel;

public class CollisionTest {

	@Test
	public void periodicTest()
	{
		Idynomics.setupSimulator("protocol/unit-tests/empty_5x5.xml");
		Compartment com = Idynomics.simulator.getCompartment("5x5");

		
		Rod rod = new Rod(
				new double[] {1.1, 1.1}, 	// first point
				new double[] {2.0, 2.0}, 	// second point
				0.5);						// radius
		rod.init(com.getShape().getCollision());

		Voxel vox = new Voxel(
				new double[] {0.6, 0.6},	// lower corner
				new double[] {1.0, 1.0});	// upper corner
		vox.init(com.getShape().getCollision());

		System.out.println( rod.collisionWith(vox) );
	}
}
