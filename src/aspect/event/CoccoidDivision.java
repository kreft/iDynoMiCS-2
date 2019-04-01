package aspect.event;

import surface.Point;
import utility.ExtraMath;
import utility.Helper;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;

import java.util.LinkedList;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.Aspect.AspectClass;
import aspect.Aspect;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import instantiable.object.InstantiableMap;

/**
 * Simple coccoid division class, divides mother cell in two with a random
 * moves mother and daughter in a random opposing direction and registers the
 * daughter cell to the compartment
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class CoccoidDivision extends Event
{
	/**
	 * The Agent's mass.
	 */
	public static String MASS = AspectRef.agentMass;
	
	/**
	 * The Agent's mass.
	 */
	public static String MASS_MAP = AspectRef.agentMassMap;
	
	/**
	 * If the Agent's mass is above this value, trigger division.
	 */
	public static String THRESHOLD_MASS = AspectRef.divisionMass;
	/**
	 * The fraction of mass that is kept by the mother Agent: 1 - this value
	 * will be inherited by the daughter cell. Must be between 0 and 1
	 * exclusive.
	 */
	public static String MUM_MASS_FRAC = AspectRef.mumMassFrac;
	/**
	 * Amount of stochastic variation used in {@code mumMassFrac}.
	 */
	public static String MUM_MASS_FRAC_CV = AspectRef.mumMassFracCV;
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
	/**
	 * Name of the plasmid loss event that should be called for the daughter
	 * agent, if specified in the agent definition.
	 */
	public String PLASMID_LOSS = AspectRef.agentPlasmidLoss;

	
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
		CoccoidDivision.transferMass(mother, daughter);
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

		if ( mother.isAspect(UPDATE_BODY) && mother.isAspect(BODY) )
		{
			mother.event(UPDATE_BODY);
			daughter.event(UPDATE_BODY);
		}
		/* Call the plasmid loss event */
		if (daughter.isAspect(PLASMID_LOSS))
			daughter.event(PLASMID_LOSS);
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
		Tier level = Tier.BULK;
		/*
		 * Find the agent-specific variable to test (mass, by default).
		 */
		
		Object mumMass = anAgent.get(this.MASS);
		double variable = Helper.totalMass(mumMass);
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
	protected static void transferMass(Agent mother, Agent daughter)
	{
		/*
		 * Find a "mother mass fraction", i.e. the fraction of mass that is
		 * kept by the mother. This value should be between 0 and 1 exclusive.
		 * 
		 * If the coefficient of variation is >0, deviate mumMassFrac using a
		 * truncated Gaussian distribution about.
		 */
		double mumMassFrac = 0.5;
		/* 5% seems like an appropriate default for cv */
		double mumMassFracCV = 0.05; 
		if ( mother.isAspect(MUM_MASS_FRAC) )
			mumMassFrac = mother.getDouble(MUM_MASS_FRAC);
		if ( mother.isAspect(MUM_MASS_FRAC_CV) )
			mumMassFracCV = mother.getDouble(MUM_MASS_FRAC_CV);
		mumMassFrac = ExtraMath.deviateFromCV(mumMassFrac, mumMassFracCV);
		/*
		 * Transfer the mass from mother to daughter, using mumMassFrac.
		 */
		Double motherMass = null, product = null;
		
		Object mumMass = mother.get(MASS);
		if ( mumMass instanceof Double && mother.getAspectType( MASS ) ==
				AspectClass.PRIMARY )
		{
			motherMass = (Double) mumMass;
			mother.set(MASS, motherMass * mumMassFrac);
			daughter.set(MASS, motherMass * (1.0 - mumMassFrac));
		}
		
		Object massMap = mother.get(MASS_MAP);
		
		if ( massMap != null && massMap instanceof Map )
		{
			@SuppressWarnings("unchecked")
			Map<String,Double> mumProducts = 
					(Map<String,Double>) massMap;
			@SuppressWarnings("unchecked")
			Map<String,Double> daughterProducts = 
					(Map<String,Double>) daughter.get(MASS_MAP);
			for ( String key : mumProducts.keySet() )
			{
				product = mumProducts.get(key);
				daughterProducts.put(key, product * (1.0-mumMassFrac) );
				mumProducts.put(key, product * mumMassFrac);
			}
			mother.set(MASS_MAP, mumProducts);
			daughter.set(MASS_MAP, daughterProducts);
		}
		if ( motherMass == null && product == null )
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
		
		double[] originalPos = momBody.getPosition(0);
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
