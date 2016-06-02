/**
 * 
 */
package testJUnit;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import boundary.Boundary;
import boundary.BoundaryLibrary.BulkBLBoundary;
import boundary.BoundaryLibrary.SolidBoundary;
import idynomics.Compartment;
import idynomics.Idynomics;
import shape.Dimension.DimName;
import utility.ExtraMath;

/**
 * 
 */
public class BoundaryTest
{
	@Test
	public void agentInsertionBoundaryLayer()
	{
		/*
		 * Test parameters.
		 */
		double tStep = 1.0;
		double tMax = 1.0;
		double compartmentLength = 20.0;
		double agentRadius = 1.0;
		// TODO set this in the boundary method
		double boundaryLayerThickness = 10.0;
		/*
		 * Set up the Simulator, Timer, and Compartment
		 */
		AllTests.setupSimulatorForTest(tStep, tMax, "agentInsertionBoundaryLayer");
		Compartment comp = Idynomics.simulator.addCompartment("oneDim");
		comp.setShape("line");
		comp.setSideLengths(new double[]{compartmentLength});
		/*
		 * The agent to be inserted: give its position as nonsense to check
		 * that it is inserted correctly.
		 */
		Agent insertAgent = new Agent();
		Body iBody = new Body(new double[]{-12345.6}, agentRadius);
		insertAgent.set(AspectRef.agentBody, iBody);
		insertAgent.set(AspectRef.isLocated, new Boolean(true));
		/*
		 * Add the agent to the boundary layer, and this to the compartment.
		 */
		Boundary bL = new BulkBLBoundary();
		bL.acceptInboundAgent(insertAgent);
		comp.addBoundary(DimName.X, 1, bL);
		/*
		 * Now make a fixed agent that the insert agent should detect.
		 */
		Agent fixedAgent = new Agent();
		Body fBody = new Body(new double[]{1.0}, 1.0);
		fixedAgent.set(AspectRef.agentBody, fBody);
		fixedAgent.set(AspectRef.isLocated, new Boolean(true));
		comp.addAgent(fixedAgent);
		/*
		 * The other boundary is unimportant, but needs to be set.
		 */
		comp.addBoundary(DimName.X, 0, new SolidBoundary());
		
		Idynomics.simulator.run();
		
		/*
		 * 
		 */
		double found = iBody.getPoints().get(0).getPosition()[0];
		double expected = compartmentLength - boundaryLayerThickness + 3*agentRadius;
		assertTrue(ExtraMath.areEqual(found, expected, 0.1*boundaryLayerThickness));
	}
}
