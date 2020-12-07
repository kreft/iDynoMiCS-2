package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.methods.DivisionMethod;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Point;

/**
 * FIXME MAP handling for this class needs updating, see CoccoidDivision as
 * example
 * rod division, taking into account periodic boundaries
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class RodDivision extends DivisionMethod {

	protected void shiftBodies(Agent mother, Agent daughter)
	{
		Shape shape = mother.getCompartment().getShape();
		Body momBody = (Body) mother.get(AspectRef.agentBody);
		/*
		 * find the closest distance between the two mass points of the rod
		 * agent and assumes this is the correct length, preventing rods being
		 * stretched out over the entire domain
		 */
		double[] midPos = shape.periodicMidPoint(
				momBody.getPosition(0), 
				momBody.getPosition(1) );
		
		double[] shift = Vector.randomPlusMinus( midPos.length, 
				0.05*(double) mother.get(AspectRef.bodyRadius) );
		
		Point p = momBody.getPoints().get(1);
		p.setPosition( shape.periodicMidPoint( 
				momBody.getPosition(0),
				Vector.add(midPos, shift) ) );
		
		Body daughterBody = (Body) daughter.get(AspectRef.agentBody);
		Point q = daughterBody.getPoints().get(0);
		q.setPosition( shape.periodicMidPoint(
				daughterBody.getPosition(1),
				Vector.minus( midPos, shift ) ) );
	}
}
