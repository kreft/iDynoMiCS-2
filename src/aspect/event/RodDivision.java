package aspect.event;

import surface.Point;
import utility.ExtraMath;
import linearAlgebra.Vector;
import shape.Shape;

import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;

/**
 * 
 * FIXME VERY DIRTY port of coccoid division class, take some time to think
 * trough and make a beautiful new class
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class RodDivision extends Event {
	
	public String MASS = AspectRef.agentMass;
	public String RADIUS = AspectRef.bodyRadius;
	public String BODY = AspectRef.agentBody;
	public String LINKED = AspectRef.agentLinks;
	public String LINKER_DIST = AspectRef.linkerDistance;
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	public String DIVIDE = AspectRef.agentDivide;

	/**
	 * Method that initiates the division
	 */
	@SuppressWarnings("unchecked")
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Tier level = Tier.BULK;
		Agent mother = (Agent) initiator;

		Shape shape = mother.getCompartment().getShape();
		
		//TODO check phase 
		double momMass =(double) mother.get(MASS);
		if ( momMass > 0.2 )
		{
			Body momBody = (Body) mother.get(BODY);

			Agent daughter = new Agent(mother); // the copy constructor
			double randM = ExtraMath.getUniRandDbl(momMass*0.5, momMass*0.55);
			mother.set(MASS, momMass-randM);
			daughter.set(MASS, randM);
			
			// FIXME think of something more robust
			// Does this create artifacts when passing two cyclic boundaries?
			/*
			 * find the closest distance between the two mass points of the rod
			 * agent and assumes this is the correct length, preventing rods being
			 * stretched out over the entire domain
			 */
			List<double[]> cyclicPoints = 
					shape.getCyclicPoints(momBody.getJoints().get(0));
			
			double[] c = cyclicPoints.get(0);
			double dist = Vector.distanceEuclid(momBody.getJoints().get(1), c);
			double dDist;
			for ( double[] d : cyclicPoints )
			{
				dDist = Vector.distanceEuclid( momBody.getJoints().get(1), d);
			
				if ( dDist < dist)
				{
					c = d;
					dist = dDist;
				}
			}
			
			double[] midPos = Vector.midPoint(c, momBody.getJoints().get(1));
			
			double[] shift = Vector.randomPlusMinus(midPos.length, 
					0.5*(double) mother.get(RADIUS));
			
			Point p = momBody.getPoints().get(1);
			p.setPosition(Vector.add(midPos, shift));
			
			Body daughterBody = (Body) daughter.get(BODY);
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(midPos, shift));


			//TODO work in progress, currently testing fillial links
			if ( ! mother.isAspect(LINKER_DIST))
			{
				if ( Log.shouldWrite(level) )
					Log.out(level, "Agent does not create fillial links");
			}
			else
			{
				LinkedList<Integer> linkers = 
						(mother.isAspect(LINKED) ? (LinkedList
						<Integer>) mother.getValue(LINKED) :
						new LinkedList<Integer>());
				linkers.add(daughter.identity());
				mother.set(LINKED, linkers);
			}
			daughter.registerBirth();
			mother.event(UPDATE_BODY);
			daughter.event(UPDATE_BODY);
			
			// if either is still larger than the div size they need to devide
			// again
			mother.event(DIVIDE);
			daughter.event(DIVIDE);
			if ( Log.shouldWrite(level) )
				Log.out(level, "RodDivision added daughter cell");
		}
	}
}
