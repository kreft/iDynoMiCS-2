package aspect.event;

import java.util.HashMap;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Compartment;
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
	
	public String INTERNAL_PRODUCTS = AspectRef.internalProducts;
	public String EPS = AspectRef.productEPS;
	public String MAX_INTERNAL_EPS = AspectRef.maxInternalEPS;
	public String EPS_SPECIES = AspectRef.epsSpecies;
	public String MASS = AspectRef.agentMass;
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;

	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		/*
		 * We can only do EPS excretion if the agent has internal products.
		 */
		if ( ! initiator.isAspect(INTERNAL_PRODUCTS) )
			return;
		/* Read in the internal products. */
		@SuppressWarnings("unchecked")
		HashMap<String,Double> internalProducts = (HashMap<String,Double>)
			initiator.getValue(INTERNAL_PRODUCTS);
		/*
		 * If there is no EPS in the internal products, then we cannot excrete.
		 */
		if ( ! internalProducts.containsKey(EPS) )
			return;
		/*
		 * Find out how much EPS the agent can hold before it much excrete.
		 */
		double maxEPS = (double) initiator.getValue(MAX_INTERNAL_EPS);
		
		if (maxEPS > internalProducts.get(EPS))
			return;
		/*
		 * Vary this number randomly by about 10%
		 */
		// TODO this should probably be set when the agent has its max EPS		
		// value set, to avoid timestep size artifacts
		double epsBlob = ExtraMath.deviateFromCV(maxEPS, 0.1);
		/*
		 * Find out how much EPS the agent has.
		 */
		double eps = internalProducts.get(EPS);
		Body body = (Body) initiator.getValue(BODY);
		String epsSpecies = initiator.getString(EPS_SPECIES);
		Compartment comp = ((Agent) initiator).getCompartment();
		while ( eps > epsBlob )
		{
			// TODO Joints state will be removed
			double[] originalPos = body.getJoints().get(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.6 * initiator.getDouble(RADIUS));
			double[] epsPos = Vector.minus(originalPos, shift);
			// FIXME this is not correct, calculate with density
			compliant = new Agent(epsSpecies, 
					new Body(new Point(epsPos),0.0),
					comp); 
			compliant.set(MASS, epsBlob);
			compliant.reg().doEvent(compliant, null, 0.0, UPDATE_BODY);
			internalProducts.put(EPS, eps - epsBlob);
			((Agent) compliant).registerBirth();

			initiator.set(INTERNAL_PRODUCTS, internalProducts);

			Log.out(Tier.BULK, "EPS particle created");
			epsBlob = maxEPS - 0.1*maxEPS*ExtraMath.getNormRand();
			eps = internalProducts.get(EPS);
		}
	}
}
