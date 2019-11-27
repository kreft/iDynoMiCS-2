package test.junit.newTests;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import agent.Agent;
import analysis.Counter;
import aspect.AspectInterface;
import boundary.Boundary;
import boundary.library.ConstantConcentrationToChemostat;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Testable;
import debugTools.Tester;
import grid.ArrayType;
import debugTools.Testable.TestMode;
import idynomics.Idynomics;

public class ReactionDiffusion implements Testable {

	String sol = "glucose";
	Compartment chemostat = null;
	Compartment biofilm = null;
	
	@Test
	public void test()
	{
		test(TestMode.UNIT);
	}
	
	public void test(TestMode mode)
	{
		/* basic simulator initiation */
		Log.set(Tier.CRITICAL);
		Idynomics.setupSimulator("protocol/unit-tests/reaction_diffusion.xml");
		Log.set(Tier.CRITICAL);
		Tester.println("reaction diffusion", mode);
		
		chemostat = Idynomics.simulator.getCompartment("chemostat");
		biofilm = Idynomics.simulator.getCompartment("biofilm");
		
		
		double dt = Idynomics.simulator.timer.getTimeStepSize();
		int i=0;
		

		while ( Idynomics.simulator.timer.isRunning() )
		{
			Tester.println("\n # " + i++, mode);
			Idynomics.simulator.step();
			for (int y = 0; y < 50; y++)
			{
				Tester.println( concn(0,y) + " \t" , mode );
					//	concnFirstOrder(y, (1000.0*0.15*0.75)/36.0, 12, 1.5E-7), mode);
			}
		}
		
	}
	
	private double concn(int x, int y)
	{

		return biofilm.environment.getSoluteGrid(sol).
				getValueAt(ArrayType.CONCN, new double[] {x,y,0});
	}
	
	private double concnFirstOrder(double y, double k, double b, double bc)
	{
		
		return ( bc * Math.exp( -Math.sqrt(k) * ( y - b ) ) * 
				( Math.exp( 2 * Math.sqrt(k) * y ) + 1 ) ) / 
				( 1 + Math.exp( 2 * b * Math.sqrt( k ) ) );
	}
}
