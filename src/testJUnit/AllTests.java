/**
 * 
 */
package testJUnit;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import idynomics.Simulator;

@RunWith(Suite.class)
@SuiteClasses({ BoundaryTest.class,
				ChemostatsTest.class,
				CoordinateMapTest.class,
				ExtraMathTest.class,
				LinearAlgebraTest.class,
				RateExpressionTest.class,
				ShapesTest.class,
				XMLableTest.class})
public class AllTests
{
	/**
	 * Numerical tolerance when comparing two {@code double} numbers for
	 * equality.
	 */
	public final static double TOLERANCE = 1E-6;
	
	/**
	 * \brief Helper method for initialising the iDynoMiCS simulator prior to
	 * running a unit test.
	 * 
	 * @param tStep Global time step length.
	 * @param tMax Simulation end time.
	 */
	public static void setupSimulatorForTest(double tStep, double tMax)
	{
		Idynomics.simulator = new Simulator();
		Idynomics.simulator.timer.setTimeStepSize(tStep);
		Idynomics.simulator.timer.setEndOfSimulation(tMax);
		Log.set(Tier.DEBUG);
	}
}
