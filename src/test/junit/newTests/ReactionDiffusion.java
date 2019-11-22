package test.junit.newTests;

import org.junit.Test;

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
		
	}
	
	private double concn(int x, int y)
	{
		return biofilm.environment.getSoluteGrid(sol).
				getValueAt(ArrayType.CONCN, new int[] {x,y});
	}
}
