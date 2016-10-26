package aspect.event;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import gereralPredicates.IsNotSpecies;
import idynomics.Compartment;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.Collision;
import surface.Point;
import surface.Surface;
import surface.predicate.AreColliding;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ExcreteEPSCumulative extends Event
{
	public String EPS = AspectRef.productEPS;
	public String MAX_INTERNAL_EPS = AspectRef.maxInternalEPS;
	public String EPS_SPECIES = AspectRef.epsSpecies;
	public String MASS = AspectRef.agentMass;
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String SPECIES = XmlRef.species;
	public String SEARCH_DIST = XmlRef.epsDist;
	
	/**
	 * The distance around the agent that we will search for existing EPS
	 * particles.
	 */
	
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		Tier level = Tier.BULK;
		
		Agent agent = (Agent) initiator;
		/*
		 * Find out how much EPS the agent has right now. If it has none, there
		 * is nothing  more to do.
		 */
		double currentEPS = this.getCurrentEPS(agent);
		if ( currentEPS == 0.0 )
		{
			if ( Log.shouldWrite(level) )
				Log.out(level, "Agent "+agent.identity()+" has no EPS");
			return;
		}
		/*
		 * Find out how much EPS the agent can hold before it must excrete.
		 */
		double maxEPS = initiator.getDouble(this.MAX_INTERNAL_EPS);
		if ( maxEPS > currentEPS )
		{
			if ( Log.shouldWrite(level) )
				Log.out(level, "Agent "+agent.identity()+" has too little EPS");
			return;
		}
		/*
		 * Vary this number randomly by about 10%
		 */
		double toExcreteEPS = ExtraMath.deviateFromCV(maxEPS * 0.5, 0.1);
		/*
		 * Find out how much EPS the agent has.
		 */
		Body body = (Body) initiator.getValue(BODY);
		List<Surface> bodySurfs = body.getSurfaces();
		String epsSpecies = initiator.getString(EPS_SPECIES);
		Compartment comp = agent.getCompartment();
		
		Collision iterator = new Collision(comp.getShape());
		
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "  Agent (ID "+agent.identity()+") has "+
					bodySurfs.size()+" surfaces,"+
					" search dist "+SEARCH_DIST);
		}
		/*
		 * Perform neighborhood search and perform collision detection and
		 * response. 
		 */
		Collection<Agent> nhbs = comp.agents.treeSearch(agent, agent.getDouble(SEARCH_DIST) );
		if ( Log.shouldWrite(level) )
			Log.out(level, "  "+nhbs.size()+" neighbors found");
		/*
		 * Remove all non-EPS neighboring agents.
		 */
		nhbs.removeIf(new IsNotSpecies(epsSpecies, this.SPECIES));
		if ( Log.shouldWrite(level) )
			Log.out(level, "  "+nhbs.size()+" of these are EPS");
		/*
		 * Remove any outside the search distance.
		 */
		Predicate<Collection<Surface>> filter = new 
				AreColliding<Collection<Surface>>(
						bodySurfs, iterator, agent.getDouble(SEARCH_DIST));
		LinkedList<Agent> epsParticles = new LinkedList<Agent>();
		for ( Agent neighbour: nhbs )
		{
			Body bodyNeighbour = ((Body) neighbour.get(this.BODY));
			List<Surface> nhbSurfs = bodyNeighbour.getSurfaces();
			if ( filter.test(nhbSurfs) )
				epsParticles.add(neighbour);
		}
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "  "+nhbs.size()+
					" of these are within the search distance");
		}
		/*
		 * Now work out where the transferred EPS will go. If there is not a
		 * suitable particle to take it, then create a new one.
		 */
		double particleMass = 0.0;
		if ( epsParticles.isEmpty() )
		{
			// TODO Joints state will be removed
			double[] originalPos = body.getJoints().get(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.6 * initiator.getDouble(this.RADIUS));
			double[] epsPos = Vector.minus(originalPos, shift);
			// FIXME this is not correct, calculate with density
			compliant = new Agent(epsSpecies, 
					new Body(new Point(epsPos),0.0),
					comp); 
			// NOTE register birth will update the body so do not leave it null
			compliant.set(this.MASS, particleMass);
			((Agent) compliant).registerBirth();
			if ( Log.shouldWrite(level) )
				Log.out(level, "EPS particle created");
		}
		else
		{
			compliant = epsParticles.get(
					ExtraMath.getUniRandInt( epsParticles.size() ) );
			particleMass = compliant.getDouble(this.MASS);
		}
		/*
		 * Whether the EPS particle is new or already present, transfer over
		 * the EPS mass from the agent to the particle.
		 */
		compliant.set(this.MASS, particleMass + toExcreteEPS);
		compliant.reg().doEvent(compliant, null, 0.0, this.UPDATE_BODY);
		currentEPS -= toExcreteEPS;
		this.updateEPS(agent, currentEPS);
	}
	
	/**
	 * \brief Ask the given agent how much EPS is has right now.
	 * 
	 * @param agent Agent.
	 * @return Mass of EPS this agent owns.
	 */
	private double getCurrentEPS(Agent agent)
	{
		/*
		 * Check first if it is just an aspect.
		 */
		if ( agent.isAspect(this.EPS) )
			return agent.getDouble(this.EPS);
		/*
		 * Check if it is part of a map of masses.
		 */
		if ( agent.isAspect(MASS) )
		{
			Object massObject = agent.getValue(this.MASS);
			if ( massObject instanceof Map )
			{
				@SuppressWarnings("unchecked")
				Map<String,Double> massMap = (Map<String,Double>) massObject;
				if ( massMap.containsKey(this.EPS) )
					return massMap.get(this.EPS);
			}
		}
		/*
		 * Assume there is no EPS.
		 */
		return 0.0;
	}
	
	/**
	 * \brief Tell the given agent to update the mass of EPS it owns.
	 * 
	 * @param agent Agent.
	 * @param newEPS New EPS mass for this agent.
	 */
	private void updateEPS(Agent agent, double newEPS)
	{
		/*
		 * Check first if it is just an aspect.
		 */
		if ( agent.isAspect(this.EPS) )
			agent.set(this.EPS, newEPS);
		/*
		 * Check if it is part of a map of masses.
		 */
		if ( agent.isAspect(this.MASS) )
		{
			Object massObject = agent.getValue(this.MASS);
			if ( massObject instanceof Map )
			{
				@SuppressWarnings("unchecked")
				Map<String,Double> massMap = (Map<String,Double>) massObject;
				if ( massMap.containsKey(this.EPS) )
				{
					massMap.put(this.EPS, newEPS);
					agent.set(this.MASS, massMap);
				}
			}
		}
	}
}
