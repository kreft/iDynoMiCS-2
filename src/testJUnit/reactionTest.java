package testJUnit;

import org.junit.Test;

import linearAlgebra.Vector;
import reaction.simple.Combined;
import reaction.simple.FirstOrder;
import reaction.simple.MichaelisMenten;
import reaction.simple.Monod;
import reaction.simple.Reaction;
import reaction.simple.Catalyst;
import reaction.simple.RateTerm;
import reaction.simple.RateTerm.ode;
import reaction.simple.SimpleInhibition;
import reaction.simple.ZeroOrder;

public class reactionTest {

	@Test
	public void testReaction()
	{
		double c0 = 2.5;
		double t  = 50.0;
		double tstep = 1.0;
		System.out.println("Michaelis Menten: lambert W function, heun's,"
				+ " euler's method");
		Reaction m = new Reaction(-1.0, new MichaelisMenten(0.25, 0.05));
		System.out.println(m.directMethod(c0, t));
		System.out.println(m.ode(c0, ode.HEUN, t, tstep));
		System.out.println(m.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\nFirst order: exact, heun's, euler's method");
		Reaction f = new Reaction(-1.0, new FirstOrder(0.02));		
		System.out.println(f.directMethod(c0, t));
		System.out.println(f.ode(c0, ode.HEUN, t, tstep));
		System.out.println(f.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\nZero order: exact, heun's, euler's method");
		Reaction z = new Reaction(-1.0, new ZeroOrder(0.02));
		System.out.println(z.directMethod(c0, t));
		System.out.println(z.ode(c0, ode.HEUN, t, tstep));
		System.out.println(z.ode(c0, ode.EULER, t, tstep));
		
		System.out.println("\ninfinite combinations! no inhibition vs inhibition");
		System.out.println("AMX biomass conc \tamonium \t\tnitrite\t\t\t oxygen");
		Reaction AMXgrowth = new Reaction(new double[]{1.0,-1.0,-1.0,0.0}, 
				new Combined( new RateTerm[]{ new Catalyst(), 
				new Monod(0.08,0.07), new Monod(0.08,0.05), 
				new SimpleInhibition(0.01)}));
		
		System.out.println(Vector.dblToString(AMXgrowth.ode(new double[]{1.0,1.0,1.0,0.0}, ode.EULER, t, tstep)));
		System.out.println(Vector.dblToString(AMXgrowth.ode(new double[]{1.0,1.0,1.0,1.0}, ode.EULER, t, tstep)));
	}
}
