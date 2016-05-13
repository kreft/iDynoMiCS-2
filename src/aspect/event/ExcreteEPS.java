package aspect.event;

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
	
	public String INTERNAL_PRODUCTS = NameRef.internalProducts;
	public String EPS = NameRef.productEPS;
	public String MAX_INTERNAL_EPS = NameRef.maxInternalEPS;
	public String EPS_SPECIES = NameRef.epsSpecies;
	public String MASS = NameRef.agentMass;
	public String UPDATE_BODY = NameRef.agentUpdateBody;
	public String BODY = NameRef.agentBody;
	public String RADIUS = NameRef.bodyRadius;

	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		if ( initiator.isAspect(INTERNAL_PRODUCTS))
		{
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProducts = 
					(HashMap<String,Double>) initiator.getValue(INTERNAL_PRODUCTS);
			
			if (internalProducts.containsKey(EPS))
			{
				double maxEPS = (double) initiator.getValue(MAX_INTERNAL_EPS);
				double epsBlob = maxEPS * ExtraMath.getUniRandDbl(0.9, 1.0);
				double eps = internalProducts.get(EPS);
				Body body = (Body) initiator.getValue(BODY);
				while ( eps > epsBlob )
				{
					// TODO Joints state will be removed
					double[] originalPos = body.getJoints().get(0);
					double[] shift = Vector.randomPlusMinus(originalPos.length, 
							0.6 * initiator.getDouble(RADIUS));
					double[] epsPos = Vector.minus(originalPos, shift);
					// FIXME this is not correct, calculate with density
					compliant = new Agent(initiator.getString(EPS_SPECIES), 
							new Body(new Point(epsPos),0.0),
							((Agent) initiator).getCompartment()); 
					compliant.set(MASS, epsBlob);
					compliant.reg().doEvent(compliant, null, 0.0, UPDATE_BODY);
					internalProducts.put(EPS, eps-epsBlob);
					((Agent) compliant).registerBirth();
					
					initiator.set(INTERNAL_PRODUCTS, internalProducts);

					Log.out(Tier.BULK, "EPS particle created");
					epsBlob = maxEPS - 0.1*maxEPS*ExtraMath.getNormRand();
					eps = internalProducts.get(EPS);
				}
			}
		}
	}
}
