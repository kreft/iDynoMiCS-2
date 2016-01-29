package agent.event.library;

import utility.ExtraMath;
import linearAlgebra.Vector;
import agent.Agent;
import agent.body.Body;
import agent.body.Point;
import agent.event.Event;

public class CoccoidDivision extends Event {
	
	// inputs 0 "mass" 1 "radius" 2 "body"

	public void start(Agent mother, Agent daughter, Double timeStep)
	{
		//TODO check phase 
		double momMass =(double) mother.get(input[0]);
		if (momMass > 0.2)
		{
			Body momBody = (Body) mother.get(input[2]);

			if (daughter == null)
				daughter = new Agent(mother); // the copy constructor
			double randHalf = ExtraMath.getUniRandDbl(momMass*0.5, momMass*0.55);
			mother.set(input[0], momMass-randHalf);
			daughter.set(input[0], randHalf);
			
			double[] originalPos = momBody.getJoints().get(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.5*(double) mother.get(input[1]));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			
			Body daughterBody = (Body) daughter.get(input[2]);
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));
			
			daughter.registerBirth();
		}
	}
	
	public Object copy() {
		return this;
	}
}
