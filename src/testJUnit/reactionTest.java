package testJUnit;

import org.junit.Test;

import reaction.simple.Firstorder;
import reaction.simple.MichaelisMenten;
import reaction.simple.Reaction.ode;
import reaction.simple.ZeroOrder;

public class reactionTest {

	@Test
	public void testReaction()
	{
		double c0 = 2.0;
		double t  = 25.0;
		double tstep = 5.0;
		System.out.println("MichaelisMenten: lambert W function, heun's,"
				+ " euler's method");
		MichaelisMenten reaction = new MichaelisMenten(0.02, 0.01);
		System.out.println(reaction.C(c0, t));
		System.out.println(reaction.ode(c0, ode.HEUN, t, tstep));
		System.out.println(reaction.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\nFirst order: exact, heun's, euler's method");
		Firstorder reaction2 = new Firstorder(0.02);
		System.out.println(reaction2.C(c0, t));
		System.out.println(reaction2.ode(c0, ode.HEUN, t, tstep));
		System.out.println(reaction2.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\nZero order: exact, heun's, euler's method");
		ZeroOrder reaction3 = new ZeroOrder(0.02);
		System.out.println(reaction3.C(c0, t));
		System.out.println(reaction3.ode(c0, ode.HEUN, t, tstep));
		System.out.println(reaction3.ode(c0, ode.EULER, t, tstep));
	}
}
