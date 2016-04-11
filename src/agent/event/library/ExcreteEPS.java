package agent.event.library;

import java.util.HashMap;

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

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ExcreteEPS extends Event
{
	// TODO move this to XmlLabel or NameRef?
	private final String EPS_TAG = "eps";
	
	public void start(AspectInterface initiator,
						AspectInterface compliant, Double timeStep)
	{
		if ( initiator.isAspect("internalProducts"))
		{
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProducts = 
			(HashMap<String,Double>) initiator.getValue("internalProducts");
			/*
			 * If this agent has EPS, keep excreting until it falls below a
			 * threshold.
			 */
			if ( internalProducts.containsKey(EPS_TAG) )
			{
				double maxEPS = (double) initiator.getValue("maxInternalEPS");
				double epsBlob = maxEPS - 0.1*maxEPS*ExtraMath.getUniRandDbl();
				double eps = internalProducts.get(EPS_TAG);
				Body body = (Body) initiator.getValue(NameRef.agentBody);
				while ( eps > epsBlob )
				{
					// TODO Joints state will be removed
					double[] originalPos = body.getJoints().get(0);
					double[] shift = Vector.randomPlusMinus(originalPos.length, 
							0.6 * initiator.getDouble(NameRef.bodyRadius));
					double[] epsPos = Vector.minus(originalPos, shift);
					// FIXME this is not correct, calculate with density
					compliant = new Agent(initiator.getString("epsSpecies"), 
							new Body(new Point(epsPos),0.0),
							((Agent) initiator).getCompartment()); 
					compliant.set("mass", epsBlob);
					compliant.reg().doEvent(compliant, null, 0.0, "updateBody");
					internalProducts.put(EPS_TAG, eps-epsBlob);
					((Agent) compliant).registerBirth();
					initiator.set("internalProducts", internalProducts);
					Log.out(Tier.BULK, "EPS particle created");
					epsBlob = maxEPS - 0.1*maxEPS*ExtraMath.getNormRand();
					eps = internalProducts.get(EPS_TAG);
				}
			}
		}

	}
}
