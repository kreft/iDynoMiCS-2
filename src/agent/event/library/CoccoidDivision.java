package agent.event.library;

import surface.Point;
import utility.ExtraMath;
import linearAlgebra.Vector;

import java.util.LinkedList;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;

/**
 * Simple coccoid division class, divides mother cell in two with a random
 * moves mother and daughter in a random opposing direction and registers the
 * daughter cell to the compartment
 * 
 * NOTE: inputs 0 "mass" 1 "radius" 2 "body"
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class CoccoidDivision extends Event {
	
	public String MASS = NameRef.agentMass;
	public String RADIUS = NameRef.bodyRadius;
	public String BODY = NameRef.agentBody;
	public String LINKED = NameRef.agentLinks;
	public String LINKER_DIST = NameRef.linkerDistance;
	public String UPDATE_BODY = NameRef.agentUpdateBody;
	public String DIVIDE = NameRef.agentDivide;
	
	public CoccoidDivision()
	{
		setInput("mass,radius,body");
	}

	/**
	 * Method that initiates the division
	 */
	@SuppressWarnings("unchecked")
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Agent mother = (Agent) initiator;

		//TODO check phase 
		double momMass =(double) mother.get(MASS);
		if ( momMass > 0.2 )
		{
			Body momBody = (Body) mother.get(BODY);

			Agent daughter = new Agent(mother); // the copy constructor
			double randM = ExtraMath.getUniRandDbl(momMass*0.5, momMass*0.55);
			mother.set(MASS, momMass-randM);
			daughter.set(MASS, randM);
			
			// TODO Joints state will be removed
			double[] originalPos = momBody.getJoints().get(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.5*(double) mother.get(RADIUS));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			
			Body daughterBody = (Body) daughter.get(BODY);
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));


			//TODO work in progress, currently testing fillial links
			if (! mother.isAspect(LINKER_DIST))
			{
				Log.out(Tier.BULK, "Agent does not create fillial "
						+ "links");
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
			
			Log.out(Tier.BULK, "CoccoidDivision added daughter cell");
		}
	}
}
