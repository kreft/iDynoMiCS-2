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
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.Idynomics;
import idynomics.Global;
import idynomics.Simulator;
import shape.Shape;
import test.junit.*;

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
		Global.updateSettings();
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
