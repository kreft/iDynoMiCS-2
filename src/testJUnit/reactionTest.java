package testJUnit;

import org.junit.Test;

import reaction.simple.FirstOrder;
import reaction.simple.MichaelisMenten;
import reaction.simple.Reaction.ode;
import reaction.simple.ZeroOrder;

public class reactionTest {

	@Test
	public void testReaction()
	{
		double c0 = 2.0;
		double t  = 20.0;
		double tstep = 10.0;
		System.out.println("Michaelis Menten: lambert W function, heun's,"
				+ " euler's method");
		MichaelisMenten reaction = new MichaelisMenten(0.02, 0.05);
		System.out.println(reaction.conc(c0, t));
		System.out.println(reaction.ode(c0, ode.HEUN, t, tstep));
		System.out.println(reaction.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\nFirst order: exact, heun's, euler's method");
		FirstOrder reaction2 = new FirstOrder(0.02);
		System.out.println(reaction2.conc(c0, t));
		System.out.println(reaction2.ode(c0, ode.HEUN, t, tstep));
		System.out.println(reaction2.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\nZero order: exact, heun's, euler's method");
		ZeroOrder reaction3 = new ZeroOrder(0.02);
		System.out.println(reaction3.conc(c0, t));
		System.out.println(reaction3.ode(c0, ode.HEUN, t, tstep));
		System.out.println(reaction3.ode(c0, ode.EULER, t, tstep));
	}
}
