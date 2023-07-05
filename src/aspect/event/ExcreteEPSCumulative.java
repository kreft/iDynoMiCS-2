package aspect.event;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import agent.Agent;
import agent.Body;
import agent.predicate.IsNotSpecies;
import aspect.AspectInterface;
import aspect.Event;
import compartment.Compartment;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.Point;
import surface.Surface;
import surface.collision.Collision;
import surface.predicate.AreColliding;
import utility.ExtraMath;
import utility.Helper;

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
	/*
	 * We could make this user settable in Global or in the protocol file. It
	 * represents how much the EPS must increase above the "max" for the agent
	 * to excrete, ensuring that we do not have many many tiny excretions in 
	 * each timestep.
	 */
	public double excessVolumeProportionAllowed = 0.1;
	
	public double excessVolumeProportionAllowedVaried;
	
	/**
	 * The distance around the agent that we will search for existing EPS
	 * particles.
	 */
	public String SEARCH_DIST = XmlRef.epsDist;
	
	
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		/*
		 * Find out how much EPS the agent has right now. If it has none, there
		 * is nothing  more to do.
		 */
		double currentEPS = this.getCurrentEPS(agent);
		if ( currentEPS == 0.0 )
		{
			return;
		}
		/*
		 * Find out how much EPS the agent can hold before it must excrete.
		 */
		double maxEPS = initiator.getDouble(this.MAX_INTERNAL_EPS);
		
		/*
		 * How much excess EPS the agent has above the "max"
		 */
		double excess = currentEPS - maxEPS;
		
		if ( excess <= 0.0 )
		{
			return;
		}
		
		if (excessVolumeProportionAllowedVaried == 0.0)
		{
			excessVolumeProportionAllowedVaried = ExtraMath.deviateFromCV(
					excessVolumeProportionAllowed, 0.1);
		}
		
		if (excess/maxEPS < excessVolumeProportionAllowedVaried)
			return;
		
		
		
		/*
		 * Now we know excretion will definitely take place deviate a little
		 * from the excess to get a value of EPS to excrete.
		 */
		double toExcreteEPS = ExtraMath.deviateFromCV(excess, 0.1);
		
		Body body = (Body) initiator.getValue(BODY);
		List<Surface> bodySurfs = body.getSurfaces();
		String epsSpecies = initiator.getString(EPS_SPECIES);
		Compartment comp = agent.getCompartment();
		
		Collision iterator = new Collision(comp.getShape());

		/*
		 * Perform neighborhood search and perform collision detection and
		 * response. 
		 */
		Collection<Agent> nhbs = comp.agents.treeSearch(agent, 
				agent.getDouble(SEARCH_DIST) );

		/*
		 * Remove all non-EPS neighboring agents.
		 */
		nhbs.removeIf(new IsNotSpecies(epsSpecies, this.SPECIES));

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

		/*
		 * Now work out where the transferred EPS will go. If there is not a
		 * suitable particle to take it, then create a new one.
		 */
		double particleMass = 0.0;
		if ( epsParticles.isEmpty() )
		{
			int numPoints = body.getNumberOfPoints();
			int randomPoint = ExtraMath.getUniRandInt(numPoints);
			double[] originalPos = body.getPosition(randomPoint);
			
			/*
			 * Ideally we should create a method in {@code surface.Rod} to 
			 * select a random point on the surface to avoid edge cases where
			 * the EPS particle is placed on or very near the rod's spine
			 */
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					initiator.getDouble(this.RADIUS));
			double[] epsPos = Vector.minus(originalPos, shift);
			// FIXME this is not correct, calculate with density
			compliant = new Agent(epsSpecies, 
					new Body(new Point(epsPos),0.0),
					comp); 
			// NOTE register birth will update the body so do not leave it null
			compliant.set(this.MASS, particleMass);
			((Agent) compliant).registerBirth();

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
		
		/*
		 * Vary this number randomly by about 10%. This will then stay fixed
		 * until an agent passes the threshold and secretes EPS. Alternatively
		 * we could make an agent aspect that holds this value so that this
		 * is specific to each agent.
		 */
		excessVolumeProportionAllowedVaried = ExtraMath.deviateFromCV(
				excessVolumeProportionAllowed, 0.1);
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
