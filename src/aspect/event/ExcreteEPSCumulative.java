package aspect.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import idynomics.Compartment;
import linearAlgebra.Vector;
import surface.Collision;
import surface.Point;
import surface.Surface;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ExcreteEPSCumulative extends Event
{
	
	public String INTERNAL_PRODUCTS = AspectRef.internalProducts;
	public String EPS = AspectRef.productEPS;
	public String MAX_INTERNAL_EPS = AspectRef.maxInternalEPS;
	public String EPS_SPECIES = AspectRef.epsSpecies;
	public String MASS = AspectRef.agentMass;
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String SPECIES = XmlRef.species;

	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		Tier level = Tier.BULK;
		
		Agent agent = (Agent) initiator;
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
		double toExcreteEPS = maxEPS * 0.5;
		/*
		 * Find out how much EPS the agent has.
		 */
		double eps = internalProducts.get(EPS);
		Body body = (Body) initiator.getValue(BODY);
		String epsSpecies = initiator.getString(EPS_SPECIES);
		Compartment comp = agent.getCompartment();
		
		Collision iterator = new Collision(comp.getShape());
		
		double searchDist = 0.5;
		
		Log.out(level, "  Agent (ID "+agent.identity()+") has "+
				body.getSurfaces().size()+" surfaces, search dist "+searchDist);
		/*
		 * Perform neighborhood search and perform collision detection and
		 * response. 
		 */
		Collection<Agent> nhbs = comp.agents.treeSearch(agent, searchDist);
		Log.out(level, "  "+nhbs.size()+" neighbors found");
		
		LinkedList<Agent> epsParticles = new LinkedList<Agent>();
		for ( Agent neighbour: nhbs )
		{
			if ( neighbour.getString(SPECIES) != epsSpecies )
				break;
			
			Body bodyNeighbour = ((Body) neighbour.get(BODY));
			List<Surface> t = bodyNeighbour.getSurfaces();

			List<Surface> s = body.getSurfaces();
			for (Surface a : s)
			{
				for (Surface b : t)
				{
					if ( iterator.distance(a, b) > searchDist )
						epsParticles.add(neighbour);
				}
			}
			
		}
		
		if (epsParticles.isEmpty())
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
			compliant.set(MASS, 0.0);
			Log.out(Tier.BULK, "EPS particle created");
		}
		else
		{
			compliant = epsParticles.get(
					ExtraMath.getUniRandInt( epsParticles.size() ) );
		}
		
		compliant.set(MASS, compliant.getDouble(MASS) + toExcreteEPS);
		compliant.reg().doEvent(compliant, null, 0.0, UPDATE_BODY);
		internalProducts.put(EPS, eps - toExcreteEPS);
		if (epsParticles.isEmpty())
			((Agent) compliant).registerBirth();

		initiator.set(INTERNAL_PRODUCTS, internalProducts);
	}
}
