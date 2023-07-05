/**
 * 
 */
package test;


import java.util.Collection;
import java.util.LinkedList;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import grid.SpatialGrid;
import idynomics.Idynomics;
import idynomics.Simulator;
import shape.Shape;
import test.junit.oldTests.AgentEventTest;
import test.junit.oldTests.BoundaryTest;
import test.junit.oldTests.ChemostatsTest;
import test.junit.oldTests.CoordinateMapTest;
import test.junit.oldTests.ExtraMathTest;
import test.junit.oldTests.IteratorForCyclicLineTests;
import test.junit.oldTests.IteratorForCyclicSquaresTests;
import test.junit.oldTests.LinearAlgebraTest;
import test.junit.oldTests.MultigridLayerForLineTests;
import test.junit.oldTests.MultigridLayerForRectangleTests;
import test.junit.oldTests.MultigridLayerForSquareTests;
import test.junit.oldTests.MultigridResolutionTests;
import test.junit.oldTests.PDEmultigridTestsForLine;
import test.junit.oldTests.PDEmultigridTestsForSquare;
import test.junit.oldTests.PdeTransientTest;
import test.junit.oldTests.RateExpressionTest;
import test.junit.oldTests.ShapesTest;
import test.junit.oldTests.XMLableTest;

@RunWith(Suite.class)
@SuiteClasses({ AgentEventTest.class,
				BoundaryTest.class,
				ChemostatsTest.class,
				CoordinateMapTest.class,
				ExtraMathTest.class,
				IteratorForCyclicLineTests.class,
				IteratorForCyclicSquaresTests.class,
				LinearAlgebraTest.class,
				MultigridLayerForLineTests.class,
				MultigridLayerForRectangleTests.class,
				MultigridLayerForSquareTests.class,
				MultigridResolutionTests.class,
				PDEmultigridTestsForLine.class,
				PDEmultigridTestsForSquare.class,
				PdeTransientTest.class,
				RateExpressionTest.class,
				ShapesTest.class,
				XMLableTest.class})
public class OldTests
{
	/**
	 * Numerical tolerance when comparing two {@code double} numbers for
	 * equality.
	 */
	public final static double TOLERANCE = 1E-6;
	public final static double TOLERANCE_SOFT = 1E-4;
	
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
		Idynomics.global.updateSettings();
		Idynomics.simulator.timer.setTimeStepSize(tStep);
		Idynomics.simulator.timer.setEndOfSimulation(tMax);
		Log.set(Tier.EXPRESSIVE);
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
	
	/**
	 * \brief Helper method for tests on solvers.
	 * 
	 * @param grids A number of SpatialGrids (1 to many)
	 * @return The given grids wrapped up in a collection.
	 */
	public static Collection<SpatialGrid> gridsAsCollection(
			SpatialGrid... grids)
	{
		Collection<SpatialGrid> gridList = new LinkedList<SpatialGrid>();
		for (SpatialGrid grid : grids)
			gridList.add(grid);
		return gridList;
	}
	
	/**
	 * \brief Helper method for creating spatial boundaries: creates an XMl
	 * element for use in instantiation.
	 * 
	 * @param extreme 0 (min) or 1 (max)
	 * @return XML element.
	 */
	public static Element getSpatialBoundaryElement(int extreme)
	{
		Document document = XmlHandler.newDocument();
		
		Element elem = document.createElement("boundary");
		elem.setAttribute("extreme", String.valueOf(extreme));
		
		return elem;
	}
}
