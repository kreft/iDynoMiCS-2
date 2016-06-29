package aspect.event;

import surface.Point;
import utility.ExtraMath;
import linearAlgebra.Vector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;

/**
 * Simple coccoid division class, divides mother cell in two with a random
 * moves mother and daughter in a random opposing direction and registers the
 * daughter cell to the compartment
 * 
 * NOTE: inputs 0 "mass" 1 "radius" 2 "body"
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class CoccoidDivision extends Event
{
	/**
	 * The Agent's mass.
	 */
	public String MASS = AspectRef.agentMass;
	/**
	 * If the Agent's mass is above this value, trigger division.
	 */
	public String THRESHOLD_MASS = AspectRef.divisionMass;
	/**
	 * The fraction of mass that is kept by the mother Agent: 1 - this value
	 * will be inherited by the daughter cell. Must be between 0 and 1
	 * exclusive.
	 */
	public String MUM_MASS_FRAC = AspectRef.mumMassFrac;
	/**
	 * Amount of stochastic variation used in {@code mumMassFrac}.
	 */
	public String MUM_MASS_FRAC_CV = AspectRef.mumMassFracCV;
	/**
	 * The Agent's body.
	 */
	public String BODY = AspectRef.agentBody;
	/**
	 * The radius of the Agent's body.
	 */
	public String RADIUS = AspectRef.bodyRadius;
	/**
	 * TODO
	 */
	public String LINKED = AspectRef.agentLinks;
	/**
	 * TODO
	 */
	public String LINKER_DIST = AspectRef.linkerDistance;
	/**
	 * Name of the update body event that both agents should call after
	 * division. 
	 */
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	/**
	 * Name of the division event that both agents should call at the end of
	 * this event, in case agents have grown a lot between time steps.
	 */
	public String DIVIDE = AspectRef.agentDivide;

	@Override
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		Tier level = Tier.BULK;
		Agent mother = (Agent) initiator;
		
		if ( ! this.shouldDivide(mother) )
			return;
		
		/*
		 * We should divide..
		 */
		
		/* Make one new agent, copied from the mother.*/
		Agent daughter = new Agent(mother);
		/* Transfer an appropriate amount of mass from mother to daughter. */
		this.transferMass(mother, daughter);
		/* Update their bodies, if they have them. */
		if ( mother.isAspect(this.BODY) && mother.isAspect(this.RADIUS) )
			this.shiftBodies(mother, daughter);
		else
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "Agent "+mother.identity()+
					" does not have a body to shift after CoccoidDivision");
			}
		}
		/* Update filial links, if appropriate. */
		if ( mother.isAspect(LINKER_DIST) )
			this.updateLinkers(mother, daughter);
		else
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "Agent "+mother.identity()+
					" does not create fillial links");
			}
		}
		/* Register the daughter's birth in the compartment they belong to. */
		daughter.registerBirth();
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "CoccoidDivision added daughter cell");
		}
		/* The bodies of both cells may now need updating. */
		mother.event(UPDATE_BODY);
		daughter.event(UPDATE_BODY);
		/* Check if either agent should divide again. */
		mother.event(DIVIDE);
		daughter.event(DIVIDE);
	}
	
	/**
	 * \brief Check if the given agent should divide now.
	 * 
	 * @param anAgent An agent.
	 * @param {@code true} if the agent should divide now, {@code false} if it
	 * should wait.
	 */
	// TODO generalise this so that the user can set the variable which
	// triggers division, and the value of this variable it should use.
	@SuppressWarnings("unchecked")
	private boolean shouldDivide(Agent anAgent)
	{
		/*
		 * Find the agent-specific variable to test (mass, by default).
		 */
		double variable = 0.0;
		Object mumMass = anAgent.get(this.MASS);
		if ( mumMass instanceof Double )
			variable = (Double) mumMass;
		else if ( mumMass instanceof double[] )
			variable = Vector.sum((double[]) mumMass);
		else if ( mumMass instanceof Map )
			for ( Object key : ((Map<String,Double>) mumMass).keySet() )
				variable += (double) ((Map<String,Double>) mumMass).get(key);
		/*
		 * Find the threshold that triggers division.
		 */
		double threshold = 0.2;
		if ( anAgent.isAspect(this.THRESHOLD_MASS) )
			threshold = anAgent.getDouble(this.THRESHOLD_MASS);
		/*
		 * Compare the two values.
		 */
		return (variable > threshold);
	}
	
	/**
	 * \brief Transfer mass from <b>mother</b> to <b>daughter</b>.
	 * 
	 * <p>By default, half the mass if transferred, but this can be overridden
	 * if the mother has <i>mumMassFrac</i> (and <i>mumMassFracCV</i>) set.</p>
	 * 
	 * @param mother Agent with too much mass.
	 * @param daughter Agent with no mass.
	 */
	private void transferMass(Agent mother, Agent daughter)
	{
		/*
		 * Find a "mother mass fraction", i.e. the fraction of mass that is
		 * kept by the mother. This value should be between 0 and 1 exclusive.
		 * 
		 * If the coefficient of variation is >0, deviate mumMassFrac using a
		 * truncated Gaussian distribution about.
		 */
		double mumMassFrac = 0.5;
		double mumMassFracCV = 0.0;
		if ( mother.isAspect(this.MUM_MASS_FRAC) )
			mumMassFrac = mother.getDouble(this.MUM_MASS_FRAC);
		if ( mother.isAspect(this.MUM_MASS_FRAC_CV) )
			mumMassFrac = mother.getDouble(this.MUM_MASS_FRAC_CV);
		mumMassFrac = ExtraMath.deviateFromCV(mumMassFrac, mumMassFracCV);
		/*
		 * Transfer the mass from mother to daughter, using mumMassFrac.
		 */
		Object mumMass = mother.get(this.MASS);
		if ( mumMass instanceof Double )
		{
			double motherMass = (Double) mumMass;
			mother.set(this.MASS, motherMass * mumMassFrac);
			daughter.set(this.MASS, motherMass * (1.0 - mumMassFrac));
		}
		else if ( mumMass instanceof double[] )
		{
			double[] motherMass = (double[]) mumMass;
			double[] daughterMass = Vector.times(motherMass, 1 - mumMassFrac);
			Vector.timesEquals(motherMass, mumMassFrac);
			mother.set(this.MASS, motherMass);
			daughter.set(this.MASS, daughterMass);
		}
		else if ( mumMass instanceof Map )
		{
			@SuppressWarnings("unchecked")
			Map<String,Double> mumProducts = (Map<String,Double>) mumMass;
			Map<String,Double> daughterProducts = new HashMap<String,Double>();
			double product;
			for ( String key : mumProducts.keySet() )
			{
				product = mumProducts.get(key);
				daughterProducts.put(key, product * (1-mumMassFrac) );
				mumProducts.put(key, product * mumMassFrac);
			}
		}
		else
		{
			Log.out(Tier.CRITICAL, "Agent "+mother.identity()+
					" has an unrecognised mass type: "+
					mumMass.getClass().toString());
		}
	}
	
	/**
	 * \brief Shift the bodies of <b>mother</b> to <b>daughter</b> in space, so
	 * that they do not overlap.
	 * 
	 * @param mother An agent.
	 * @param daughter Another agent, whose body overlaps a lot with that of
	 * <b>mother</b>.
	 */
	private void shiftBodies(Agent mother, Agent daughter)
	{
		Body momBody = (Body) mother.get(BODY);
		Body daughterBody = (Body) daughter.get(BODY);
		
		// TODO Joints state will be removed
		double[] originalPos = momBody.getJoints().get(0);
		double[] shift = Vector.randomPlusMinus(originalPos.length, 
				0.5*mother.getDouble(RADIUS));
		
		Point p = momBody.getPoints().get(0);
		p.setPosition(Vector.add(originalPos, shift));
		Point q = daughterBody.getPoints().get(0);
		q.setPosition(Vector.minus(originalPos, shift));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param mother
	 * @param daughter
	 */
	//TODO work in progress, currently testing filial links
	@SuppressWarnings("unchecked")
	private void updateLinkers(Agent mother, Agent daughter)
	{
		/*
		 * If this mother can link, add the daughter to her list of
		 * links and update.
		 */
		LinkedList<Integer> linkers;
		if ( mother.isAspect(LINKED) )
			linkers = (LinkedList<Integer>) mother.getValue(LINKED);
		else
			linkers = new LinkedList<Integer>();
		linkers.add(daughter.identity());
		// TODO add the mother to the daughter's links?
		// TODO presumably, the daughter's linkers were copied directly
		// from the mother... is this appropriate?
		mother.set(LINKED, linkers);
	}
}
