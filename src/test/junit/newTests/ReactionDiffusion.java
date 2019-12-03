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
		Tester.println("reaction diffusion test", mode);
		
		chemostat = Idynomics.simulator.getCompartment("chemostat");
		biofilm = Idynomics.simulator.getCompartment("biofilm");
		double solver, model, error;
		double mre1 = 0, mre2 = 0;
		int hBiofilm = 10, hDBL = hBiofilm+30;
		int e5 = 0, e10 = 0, e20 = 0;
		
		int i=0;
		while ( Idynomics.simulator.timer.isRunning() )
		{
			i++;
			Idynomics.simulator.step();

			if(i > 1)
			{
				Tester.println( "Solver \t\t\t Symbolic model "
						+ "\t Relative error \t Location", mode);
				for (int y = 0; y < hDBL+1; y++)
				{
					solver = concn(0,y);
					if(y <= hBiofilm)
					{
						model = concnFirstOrder(y, (100000.0*0.15*0.8)/
								36000.0, hBiofilm, concn(0, hBiofilm));
						error = (1-solver/model);
						Tester.println( solver + "\t " + model + "\t " +
								error + "\t film", mode);
						mre1 += Math.abs(error);
						if(Math.abs(error) > 0.05)
							e5++;
						if(Math.abs(error) > 0.10)
							e10++;
						if(Math.abs(error) > 0.20)
							e20++;
					}
					else if(y > hBiofilm && y <= hDBL)
					{
						model = concnDiff(y, hBiofilm, concn(0, hBiofilm), 
								hDBL, concn(0, hDBL));
						error = (1-solver/model);
						Tester.println( solver + "\t " + model + "\t " +
								error + "\t DBL", mode);
						mre2 += Math.abs(error);						
						if(Math.abs(error) > 0.05)
							e5++;
						if(Math.abs(error) > 0.10)
							e10++;
						if(Math.abs(error) > 0.20)
							e20++;
					}
				}
			}
		}
		mre1 /= hBiofilm;
		mre2 /= hDBL-hBiofilm;
		

		Tester.println("\n", mode);

		Tester.assess(0.0, mre1, 0.1 , mode, 
				"Mean Relative error biofilm < 0.1");
		Tester.assess(0.0, mre2, 0.1 , mode, 
				"Mean Relative error diffusion boundary layer < 0.1");
		Tester.assess(0.0, (mre1+mre2)/2, 0.1 , mode, 
				"Mean Relative error overall < 0.1");

		Tester.println("\n", mode);
		Tester.println("#n |Relative error| > .05 = " + e5 , mode);
		Tester.println("#n |Relative error| > .10 = " + e10 , mode);
		Tester.println("#n |Relative error| > .20 = " + e20 , mode);
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
	
	private double concnDiff(double y, double b, double bc, double t, double tc)
	{
		return ( (tc-bc)*y + (bc*t - tc*b) ) / ( t-b );
	}
}
