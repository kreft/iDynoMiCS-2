package agent.event.library;

import surface.Link;
import surface.Point;
import surface.Surface;
import utility.ExtraMath;
import linearAlgebra.Vector;

import java.util.LinkedList;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.NameRef;

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
		double momMass =(double) mother.get(input[0]);
		if ( momMass > 0.2 )
		{
			Body momBody = (Body) mother.get(input[2]);

			Agent daughter = new Agent(mother); // the copy constructor
			double randM = ExtraMath.getUniRandDbl(momMass*0.5, momMass*0.55);
			mother.set(input[0], momMass-randM);
			daughter.set(input[0], randM);
			
			// TODO Joints state will be removed
			double[] originalPos = momBody.getJoints().get(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.5*(double) mother.get(input[1]));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			
			Body daughterBody = (Body) daughter.get(input[2]);
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));
			
			
			//TODO work in progress, currently testing fillial links
			if (! mother.isAspect("linkerDist"))
			{
				Log.out(Tier.BULK, "Agent does not create fillial "
						+ "links");
			}
			else
			{
				LinkedList<Integer> linkers = 
						(mother.isAspect("linkedAgents") ? (LinkedList
						<Integer>) mother.getValue("linkedAgents") :
						new LinkedList<Integer>());
				linkers.add(daughter.identity());
				mother.set("linkedAgents", linkers);
			}
			daughter.registerBirth();
			mother.event("updateBody");
			daughter.event("updateBody");
			
			// if either is still larger than the div size they need to devide
			// again
			mother.event("divide");
			daughter.event("divide");
			
			Log.out(Tier.BULK, "CoccoidDivision added daughter cell");
		}
	}
}
