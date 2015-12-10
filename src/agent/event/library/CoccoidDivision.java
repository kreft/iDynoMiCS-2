package agent.event.library;

import utility.ExtraMath;
import linearAlgebra.Vector;
import agent.Agent;
import agent.body.Body;
import agent.body.Point;
import agent.event.Event;

public class CoccoidDivision implements Event {

	public void start(Agent mother, Agent daughter, Double timeStep)
	{
		//TODO check phase 
		double momMass =(double) mother.get("mass");
		if (momMass > 1)
		{
			Body momBody = (Body) mother.get("body");

			if (daughter == null)
				daughter = new Agent(mother); // the copy constructor
			double randHalf = ExtraMath.getUniRandDbl(momMass*0.5, momMass*0.55);
			mother.set("mass", momMass-randHalf);
			daughter.set("mass", randHalf);
			
			double[] originalPos = momBody.getJoints().get(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.5*(double) mother.get("radius"));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			
			Body daughterBody = (Body) daughter.get("body");
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));
			
			daughter.registerBirth();
		}
	}
}
