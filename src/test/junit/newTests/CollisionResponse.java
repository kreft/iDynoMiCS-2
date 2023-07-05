package test.junit.newTests;

import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.Testable;
import debugTools.Tester;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import org.junit.Test;
import processManager.library.AgentRelaxation;
import surface.*;

import java.util.LinkedList;

/**
 * \brief: Unit tests for collision response.
 *
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public class CollisionResponse implements Testable {
	
	@Test
	public void test()
	{
		test(TestMode.UNIT);
	}
	
	public void test(TestMode mode)
	{
		/* basic simulator initiation */
		Log.set(Tier.CRITICAL);
		Idynomics.setupSimulator("protocol/unit-tests/simple_relax.xml");
		Compartment com = null;

		double femtoNewton = 3.6E9;

		Idynomics.simulator.initialRun();
		int i=0;
		LinkedList<Double> res = new LinkedList<Double>();
		while ( Idynomics.simulator.timer.isRunning() )
		{
			Idynomics.simulator.step();
			String output = Log.stringValue();
			int index = output.lastIndexOf("residualStress" );
			int longest = 36;
			double tmp = -1.0;
			while ( longest > 15) {
				if( tmp != -1.0)
					break;
				try {
					tmp = Double.valueOf(output.substring(index + 15, index + longest));
				} catch (NumberFormatException e)
				{
					longest--;
				}
			}
			res.add(tmp);
			i++;
//			if( tmp < 0.1*femtoNewton )
//				break;
		}
		for( Double d : res )
			System.out.println( d/femtoNewton );
	}
}
