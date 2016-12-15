/**
 * 
 */
package test;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import idynomics.Param;
import idynomics.Simulator;
import shape.Shape;
import test.junit.AgentEventTest;
import test.junit.BoundaryTest;
import test.junit.ChemostatsTest;
import test.junit.CoordinateMapTest;
import test.junit.ExtraMathTest;
import test.junit.LinearAlgebraTest;
import test.junit.PdeTest;
import test.junit.RateExpressionTest;
import test.junit.ShapesTest;
import test.junit.XMLableTest;

@RunWith(Suite.class)
@SuiteClasses({ AgentEventTest.class,
				BoundaryTest.class,
				ChemostatsTest.class,
				CoordinateMapTest.class,
				ExtraMathTest.class,
				LinearAlgebraTest.class,
				PdeTest.class,
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
	public static void setupSimulatorForTest(double tStep, double tMax, String name)
	{
		Idynomics.simulator = new Simulator();
		Idynomics.global.outputRoot = "./unitTests";
		Idynomics.global.simulationName = name;
		Param.setOutputLocation();
		Idynomics.simulator.timer.setTimeStepSize(tStep);
		Idynomics.simulator.timer.setEndOfSimulation(tMax);
		Log.set(Tier.DEBUG);
		Log.setupFile();
	}
	
	/**
	 * \brief Helper method for shape instantiation.
	 * 
	 * @param name Name of the shape (must begin with a capital letter).
	 * @return Object of the corresponding shape class.
	 */
	public static Shape GetShape(String name)
	{
		Shape shape = null;
		String fullName = "shape.ShapeLibrary$" + name;
		try
		{
			shape = (Shape) Class.forName(fullName).newInstance();
		}
		catch (InstantiationException | IllegalAccessException |
				ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return shape;
	}
}
