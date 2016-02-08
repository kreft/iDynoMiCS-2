package agent.event.library;

import surface.Link;
import surface.Point;
import surface.Surface;
import utility.ExtraMath;
import linearAlgebra.Vector;
import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.tier;
import idynomics.NameRef;

/**
 * Simple coccoid division class, divides mother cell in two with a random
 * moves mother and daughter in a random opposing direction and registers the
 * daughter cell to the compartment
 * @author baco
 *
 * NOTE: inputs 0 "mass" 1 "radius" 2 "body"
 */
public class CoccoidDivision extends Event {

	/**
	 * Method that initiates the division
	 */
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Agent mother = (Agent) initiator;
		Agent daughter = (Agent) compliant;
		//TODO check phase 
		double momMass =(double) mother.get(input[0]);
		if (momMass > 0.2)
		{
			Body momBody = (Body) mother.get(input[2]);

			// FIXME change for settable random factor
			if (daughter == null)
				daughter = new Agent(mother); // the copy constructor
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
			if (mother.get(NameRef.fillialLinker) == null || !(boolean) 
					mother.get(NameRef.fillialLinker))
			{
				Log.out(tier.BULK, "Agent does not create fillial "
						+ "links");
			}
			else
			{
				momBody._links.add( new Link( new Point[]{ p , q } , 
						new Surface[]{ momBody.getSurface(),
						daughterBody.getSurface() } , 1.7 ));
			}
			daughter.registerBirth();
			Log.out(tier.DEBUG, "CoccoidDivision added daughter cell");
		}
	}
}
