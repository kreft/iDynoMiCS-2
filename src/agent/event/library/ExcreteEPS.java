package agent.event.library;

import java.util.HashMap;
import java.util.LinkedList;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.NameRef;
import linearAlgebra.Vector;
import surface.Point;
import utility.ExtraMath;

public class ExcreteEPS   extends Event {

	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		if ( initiator.isAspect("internalProducts"))
		{
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProducts = 
					(HashMap<String,Double>) initiator.getValue("internalProducts");
			
			if (internalProducts.containsKey("eps"))
			{
				double eps = internalProducts.get("eps");
				if (eps > initiator.getDouble("maxInternalEPS"))
				{
					// TODO Joints state will be removed
					double[] originalPos = ((Body) initiator.getValue(NameRef.agentBody)).getJoints().get(0);
					double[] shift = Vector.randomPlusMinus(originalPos.length, 
							0.6 * initiator.getDouble(NameRef.bodyRadius));
					double[] epsPos = Vector.minus(originalPos, shift);
					
					// FIXME this is not correct, calculate with density
					compliant = new Agent(initiator.getString("epsSpecies"), 
							new Body(new Point(epsPos),ExtraMath.radiusOfASphere(eps)),
							((Agent) initiator).getCompartment()); // the copy constructor
					compliant.set("mass", eps);
					internalProducts.put("eps",0.0);
					((Agent) compliant).registerBirth();
					
					initiator.set("internalProducts", internalProducts);

					Log.out(Tier.BULK, "EPS particle created");
				}
			}
		}
		
	}
}
