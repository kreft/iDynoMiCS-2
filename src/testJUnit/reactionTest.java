package testJUnit;

import org.junit.Test;

import linearAlgebra.Vector;
import reaction.Reaction;
import reaction.term.Catalyst;
import reaction.term.Combined;
import reaction.term.FirstOrder;
import reaction.term.MichaelisMenten;
import reaction.term.Monod;
import reaction.term.RateTerm;
import reaction.term.SimpleInhibition;
import reaction.term.ZeroOrder;

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
		System.out.println(m.ode(c0, Reaction.ode.HEUN, t, tstep));
		System.out.println(m.ode(c0, Reaction.ode.EULER, t, tstep));
		
		System.out.println("\nFirst order: exact, heun's, euler's method");
		Reaction f = new Reaction(-1.0, new FirstOrder(0.02));		
		System.out.println(f.directMethod(c0, t));
		System.out.println(f.ode(c0, Reaction.ode.HEUN, t, tstep));
		System.out.println(f.ode(c0, Reaction.ode.EULER, t, tstep));
		
		System.out.println("\nZero order: exact, heun's, euler's method");
		Reaction z = new Reaction(-1.0, new ZeroOrder(0.02));
		System.out.println(z.directMethod(c0, t));
		System.out.println(z.ode(c0, Reaction.ode.HEUN, t, tstep));
		System.out.println(z.ode(c0, Reaction.ode.EULER, t, tstep));
		
		System.out.println("\ninfinite combinations! no inhibition vs "
				+ "inhibition: \"real world\" Anammox example");
		System.out.println("AMX biomass conc \tamonium \t\tnitrite\t\t\toxygen"
				+ "\tnitrate \t\tdinitrogen");
		Reaction AMXgrowth = new Reaction(new double[]{
				1.0,			//Anammox
				-6.359308176,	//Ammonium
				-7.166501159,	//Nitrite
				0.0,			//Oxygen
				0.877192982,	//Nitrate
				12.57861635}, 	//Dinitrogen
				new Combined( new RateTerm[]{ new Catalyst(), 
				new Monod(0.08,0.07), new Monod(0.08,0.05), 
				new SimpleInhibition(0.01)}));
		
		System.out.println(Vector.dblToString(AMXgrowth.ode(new double[]{
				1.0,1.0,1.0,0.0,0.0,0.0}, Reaction.ode.EULER, t, tstep)));
		System.out.println(Vector.dblToString(AMXgrowth.ode(new double[]{
				1.0,1.0,1.0,1.0,0.0,0.0}, Reaction.ode.EULER, t, tstep)));
	}
}
