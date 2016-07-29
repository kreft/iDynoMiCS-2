/**
 * 
 */
package testJUnit;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import agent.Agent;
import agent.Body;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import boundary.spatialLibrary.SolidBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Compartment;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
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
		String compName = "oneDim";
		/*
		 * Set up the Simulator, Timer, and Compartment
		 */
		AllTests.setupSimulatorForTest(tStep, tMax, "agentInsertionBoundaryLayer");
		Compartment comp = Idynomics.simulator.addCompartment(compName);
		comp.setShape("line");
		comp.setSideLengths(new double[]{compartmentLength});
		/*
		 * The agent to be inserted: give its position as nonsense to check
		 * that it is inserted correctly. To prevent the random walk, we set
		 * the agent pull distance to the boundary layer thickness.
		 */
		Agent insertAgent = new Agent();
		Body iBody = new Body(new double[]{-12345.6}, agentRadius);
		insertAgent.set(AspectRef.agentBody, iBody);
		insertAgent.set(AspectRef.isLocated, new Boolean(true));
		insertAgent.set(AspectRef.agentCurrentPulldistance, boundaryLayerThickness);
		/*
		 * Add the agent to the boundary layer, and this to the compartment.
		 */
		BiofilmBoundaryLayer bL = new BiofilmBoundaryLayer(DimName.X, 1);
		bL.init(comp.environment, comp.agents, compName);
		bL.acceptInboundAgent(insertAgent);
		bL.setLayerThickness(boundaryLayerThickness);
		comp.addBoundary(bL);
		/*
		 * Now make a fixed agent that the insert agent should detect.
		 */
		Agent fixedAgent = new Agent(comp);
		Body fBody = new Body(new double[]{1.0}, 1.0);
		Log.out(Tier.DEBUG, "Agent (UID: "+fixedAgent.identity()+") at x = 1.0");
		fixedAgent.set(AspectRef.agentBody, fBody);
		fixedAgent.set(AspectRef.isLocated, new Boolean(true));
		comp.addAgent(fixedAgent);
		/*
		 * The other boundary is unimportant, but needs to be set.
		 */
		comp.addBoundary(new SolidBoundary(DimName.X, 0));
		/*
		 * Make sure that the shape's boundaries have the right surfaces.
		 */
		comp.getShape().setSurfaces();
		
		Idynomics.simulator.run();
		
		/*
		 * 
		 */
		double found = iBody.getPoints().get(0).getPosition()[0];
		Log.out(Tier.DEBUG, "Agent (UID: "+insertAgent.identity()+") at x = "+found);
		double expected = compartmentLength - boundaryLayerThickness + 3*agentRadius;
		assertTrue(ExtraMath.areEqual(found, expected, 0.1*boundaryLayerThickness));
	}
}
