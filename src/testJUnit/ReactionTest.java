package testJUnit;

import java.util.HashMap;

import org.junit.Test;

import expression.Addition;
import expression.Component;
import expression.Constant;
import expression.Division;
import expression.Variable;
import linearAlgebra.Vector;
import zArchivedClasses.Catalyst;
import zArchivedClasses.Combined;
import zArchivedClasses.FirstOrder;
import zArchivedClasses.MichaelisMenten;
import zArchivedClasses.Monod;
import zArchivedClasses.RateTerm;
import zArchivedClasses.Reaction;
import zArchivedClasses.SimpleInhibition;
import zArchivedClasses.ZeroOrder;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ReactionTest
{

	@Test
	public void testReaction()
	{
		double c0 = 2.5;
		double t  = 15.0;
		double tstep = 0.1;
		System.out.println("Michaelis Menten: lambert W function, heun's,"
				+ " euler's method");
		Reaction m = new Reaction(-1.0, " ",new MichaelisMenten(0.25, 0.05));
		System.out.println(m.directMethod(c0, t));
		System.out.println(m.ode(c0, Reaction.ode.HEUN, t, tstep));
		System.out.println(m.ode(c0, Reaction.ode.EULER, t, tstep));
		
		System.out.println("\nFirst order: exact, heun's, euler's method");
		Reaction f = new Reaction(-1.0, " ",new FirstOrder(0.02));		
		System.out.println(f.directMethod(c0, t));
		System.out.println(f.ode(c0, Reaction.ode.HEUN, t, tstep));
		System.out.println(f.ode(c0, Reaction.ode.EULER, t, tstep));
		
		System.out.println("\nZero order: exact, heun's, euler's method");
		Reaction z = new Reaction(-1.0, " ",new ZeroOrder(0.02));
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
				new String[]{"Anammox",
				"Ammonium",
				"Nitrite",
				"Oxygen",
				"Nitrate",
				"Dinitrogen"},
				new Combined( new RateTerm[]{ new Catalyst(), 
				new Monod(0.07), new Monod(0.05), 
				new SimpleInhibition(0.01), new ZeroOrder(0.08)}));
		
		System.out.println(Vector.toString(AMXgrowth.ode(new double[]{
				1.0, 1.0, 1.0, 0.0, 0.0, 0.0}, Reaction.ode.EULER, t, tstep)));
		System.out.println(Vector.toString(AMXgrowth.ode(new double[]{
				1.0, 1.0, 1.0, 1.0, 0.0, 0.0}, Reaction.ode.EULER, t, tstep)));
		
		System.out.println(AMXgrowth.reaction());
	}
	

	
	@Test
	public void expres()
	{
		HashMap<String,Double> values = new HashMap<String,Double>();
		values.put("S", 1.0);

		Component s = new Variable("S");
		Component k = new Constant("k",0.07);
		Component d = new Addition(s,k);
		Component f = new Division(s,d);
		for( int i = 0; i < 10000; i++) {

		System.out.println(f.getValue(values));
		}
	}
	
	@Test
	public void expres3()
	{
		for( int i = 0; i < 10000; i++) {
			System.out.println(1.0/(0.07+1));
		}
	}
	
	@Test
	public void expres2()
	{
		for( int i = 0; i < 10000; i++) {
			double[] t = new double[]{1.0};
			Reaction AMXgrowth = new Reaction(new double[]{1.0, 0.07},new String[]{"Anammox",
					"Ammonium"},new Combined( new RateTerm[]{ new Catalyst(), 
					new Monod(0.07)}));
			System.out.println(AMXgrowth.rate(t));
		}
	}
}
