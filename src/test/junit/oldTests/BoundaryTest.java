/**
 * 
 */
package test.junit.oldTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import boundary.spatialLibrary.SolidBoundary;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import test.OldTests;
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
		String compName = "oneDim", dummyCompName = "dummy";
		/*
		 * Set up the Simulator, Timer, and Compartments
		 */
		OldTests.setupSimulatorForTest(tStep, tMax, "agentInsertionBoundaryLayer");
		Compartment comp = Idynomics.simulator.addCompartment(compName);
		Shape shape = OldTests.GetShape("Line");
		comp.setShape(shape);
		comp.setSideLengths(new double[]{compartmentLength});
		/* Dummy compartment */
		Compartment dummyComp = Idynomics.simulator.addCompartment(dummyCompName);
		Shape dummyShape = OldTests.GetShape("Dimensionless");
		dummyComp.setShape(dummyShape);
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
		Dimension x = shape.getDimension(DimName.X);
		BiofilmBoundaryLayer bL = new BiofilmBoundaryLayer();
		Element elem = OldTests.getSpatialBoundaryElement(1);
		elem.setAttribute(XmlRef.layerThickness, "10.0");
		elem.setAttribute(XmlRef.partnerCompartment, dummyCompName);
		bL.instantiate(elem, x);
		bL.setContainers(comp.environment, comp.agents);
		//TODO - rewrite this test
		//bL.acceptInboundAgent(insertAgent);
		bL.setLayerThickness(boundaryLayerThickness);
		comp.addBoundary(bL);
		/*
		 * Now make a fixed agent that the insert agent should detect.
		 */
		Agent fixedAgent = new Agent();
		fixedAgent.setCompartment(comp);
		Body fBody = new Body(new double[]{1.0}, 1.0);
		Log.out(Tier.DEBUG, "Agent (UID: "+fixedAgent.identity()+") at x = 1.0");
		fixedAgent.set(AspectRef.agentBody, fBody);
		fixedAgent.set(AspectRef.isLocated, new Boolean(true));
		comp.addAgent(fixedAgent);
		/*
		 * The other boundary is unimportant, but needs to be set.
		 */
		SolidBoundary solid = new SolidBoundary();
		solid.setParent(x);
		solid.setExtreme(0);
		comp.addBoundary(solid);
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
