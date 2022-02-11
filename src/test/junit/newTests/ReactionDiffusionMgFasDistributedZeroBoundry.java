package test.junit.newTests;

import boundary.Boundary;
import boundary.WellMixedBoundary;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Testable;
import debugTools.Tester;
import grid.ArrayType;
import idynomics.Idynomics;
import org.junit.Test;

public class ReactionDiffusionMgFasDistributedZeroBoundry implements Testable {

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
		String file = "protocol/unit-tests/reaction_diffusion_smooth_distribution_0_boundry.xml";
		
		/* basic simulator initiation */
		Log.set(Tier.CRITICAL);
		Idynomics.setupSimulator(file);
		Log.set(Tier.CRITICAL);
		
		Tester.println("reaction diffusion test", mode);
		chemostat = Idynomics.simulator.getCompartment("chemostat");
		biofilm = Idynomics.simulator.getCompartment("biofilm");

//		/* set height of the biofilm and the diffusion boundary layer */
//		int hBiofilm = 16, hDBL = hBiofilm+20, x=0;
//		/* max accepted Mean absolute relative error. As it is quite difficult
//		 * to get to precisely line-out the biofilm height with the grid and to
//		 * get a precise packing density typically 15% would be a good result */
//		double acceptedMRE = 0.15;
//		/* polling frequency, set equal to the grid resolution (or a multitude), 
//		 * does not go below 1 */
//		int polling = 2;

		/* collect some values from the compartment to make life a bit easier
		 * when testing different settings */
		double bLayer= 0.0;
		for(Boundary b : biofilm.getShape().getAllBoundaries())
			if(b instanceof WellMixedBoundary)
				bLayer = ((WellMixedBoundary) b).getLayerThickness();
		
		double[] vLength = new double[3];
		biofilm.getShape().getVoxelSideLengthsTo(vLength, new int[3]);

		/* set height of the biofilm and the diffusion boundary layer */
		int hBiofilm = 31, hDBL = (int) (hBiofilm+bLayer), x=0;
		/* packing density of catalyst *cylinders* */
		double pakcing = 0.5/(2.0*2.0);
		/* max accepted Mean absolute relative error. As it is quite difficult
		 * to get to precisely line-out the biofilm height with the grid and to
		 * get a precise packing density typically 15% would be a good result */
		double acceptedMRE = 0.025;
		/* polling frequency, set equal to the grid resolution (or a multitude), 
		 * does not go below 1 */
		int polling = Math.max((int)vLength[0], 1);
		
		/* setting up internally used variables */
		double solver, model, error;
		double mre1 = 0, mre2 = 0;
		int e5 = 0, e10 = 0, e20 = 0;
		int i=0;
		boolean nodesystem = true;

		Idynomics.simulator.initialRun();

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
					boolean poll;
					if( nodesystem )
						poll = ( (y%polling)-1 == 0);
					else
						poll = (y%polling == 0);
					solver = concn(x ,y);
					if(y <= hBiofilm && poll)
					{
						/* catalyst part */
						model = concnFirstOrder( y, ((2.0E-02/35.0E-06)*2.63*pakcing) /
								36000.0, hBiofilm, 2e-6 ); // since we dont use bl here we can just write bulk 2e-6 instead of concn(x, hBiofilm)
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
				}
			}
		}
		mre1 /= hBiofilm/polling;
		mre2 /= (hDBL-hBiofilm)/polling;
		
		/* amount of times the relative error exceeds .05, .10 and .20 */
		Tester.println("\n", mode);
		Tester.println("#n |Relative error| > .05 = " + e5 , mode);
		Tester.println("#n |Relative error| > .10 = " + e10 , mode);
		Tester.println("#n |Relative error| > .20 = " + e20 , mode);

		/* Unit test: are the MREs close to 0? */
		Tester.println("\n", mode);
		Tester.assess(0.0, mre1, acceptedMRE , mode, 
				"Mean Relative error biofilm "  + (float)mre1 + " < " + acceptedMRE);
	}
	/**
	 * Poll concentration in biofilm compartment.
	 * 
	 * @param x location
	 * @param y location
	 * @return conentration at location (x,y,z=0)
	 */
	private double concn(int x, int y)
	{
		return biofilm.environment.getSoluteGrid(sol).
				getValueAt(ArrayType.CONCN, new double[] {x,y,0});
	}
	/**
	 * calculate concentration profile based on symbolic solution 1D first order
	 * reaction diffusion.
	 * 
	 * @param y location
	 * @param k reaction constant
	 * @param b height of upper boundary
	 * @param bc concentration at upper boundary
	 * @return concentration at location y
	 */
	private double concnFirstOrder(double y, double k, double b, double bc)
	{
		return ( bc * Math.exp( -Math.sqrt(k) * ( y - b ) ) * 
				( Math.exp( 2 * Math.sqrt(k) * y ) + 1 ) ) / 
				( 1 + Math.exp( 2 * b * Math.sqrt( k ) ) );
	}
	/**
	 * Calculate concentration profile based on simple 1D diffusion model.
	 * 
	 * @param y location
	 * @param b starting height
	 * @param bc concentration at starting height
	 * @param t top height
	 * @param tc concentration at top height
	 * @return concentration at location y
	 */
	private double concnDiff(double y, double b, double bc, double t, double tc)
	{
		return ( (tc-bc)*y + (bc*t - tc*b) ) / ( t-b );
	}
}
