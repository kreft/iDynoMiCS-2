package test.junit.newTests;

import org.junit.Test;

import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Testable;
import debugTools.Tester;
import grid.ArrayType;
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
			int hBiofilm = 10;
			if(i > 1)
			{
				for (int y = 0; y < 30; y++)
				{
					if(y <= hBiofilm)
						Tester.println( concn(0,y) + " \t" +
								concnFirstOrder(y, (100000.0*0.15*0.8)/
								36000.0, hBiofilm, concn(0, hBiofilm)), mode);
					else
						Tester.println( concn(0,y) + " \t", mode);
				}

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
