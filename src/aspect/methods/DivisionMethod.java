package aspect.methods;

import java.util.LinkedList;
import java.util.Map;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import aspect.Aspect.AspectClass;
import dataIO.Log;
import dataIO.Log.Tier;
import referenceLibrary.AspectRef;
import utility.ExtraMath;
import utility.Helper;

public abstract class DivisionMethod extends Event {
	
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		if ( ! this.shouldDivide(initiator) )
			return;

		/* Make one new agent, copied from the mother.*/
		compliant = new Agent((Agent) initiator);
		/* Transfer an appropriate amount of mass from mother to daughter. */
		DivisionMethod.transferMass(initiator, compliant);
		/* Update their bodies, if they have them. */
		if ( initiator.isAspect(AspectRef.agentBody) &&
				initiator.isAspect(AspectRef.bodyRadius) )
			shiftBodies((Agent) initiator,(Agent) compliant);

		/* The bodies of both cells may now need updating. */
		updateAgents((Agent) initiator,(Agent) compliant);
	}
	
	/**
	 * Classes extending DivisionMethod must specify how newly formed agents
	 * are positioned.
	 * 
	 * @param mother
	 * @param daughter
	 */
	protected abstract void shiftBodies(Agent mother, Agent daughter);
	
	/**
	 * \brief Check if the given agent should divide now.
	 * 
	 * @param anAgent An agent.
	 * @param {@code true} if the agent should divide now, {@code false} if it
	 * should wait.
	 */
	protected boolean shouldDivide(AspectInterface initiator)
	{
		/* Find the agent-specific variable to test (mass, by default).	 */
		Object iniMass = initiator.getValue( AspectRef.agentMass );
		double variable = Helper.totalMass( iniMass );
		double threshold = Double.MAX_VALUE;
		if ( initiator.isAspect( AspectRef.divisionMass ))
			threshold = initiator.getDouble( AspectRef.divisionMass );
		return ( variable > threshold );
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
	protected static void transferMass(AspectInterface initiator,
			AspectInterface compliant)
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
		if ( initiator.isAspect(AspectRef.mumMassFracCV) )
			mumMassFrac = initiator.getDouble(AspectRef.mumMassFracCV);
		if ( initiator.isAspect(AspectRef.mumMassFracCV) )
			mumMassFracCV = initiator.getDouble(AspectRef.mumMassFracCV);
		mumMassFrac = ExtraMath.deviateFromCV(mumMassFrac, mumMassFracCV);
		/*
		 * Transfer the mass from mother to daughter, using mumMassFrac.
		 */
		Double motherMass = null, product = null;
		
		Object mumMass = initiator.getValue(AspectRef.agentMass);
		if ( mumMass instanceof Double && initiator.getType(
				AspectRef.agentMass ) == AspectClass.PRIMARY )
		{
			motherMass = (Double) mumMass;
			initiator.set(AspectRef.agentMass, motherMass * mumMassFrac);
			compliant.set(AspectRef.agentMass, motherMass * (1.0 - mumMassFrac));
		} else 	if ( mumMass instanceof Map )
		{
			String ref = AspectRef.agentMass;
			if ( ref != null )
			{
				@SuppressWarnings("unchecked")
				Map<String,Double> mumProducts = 
						(Map<String,Double>) initiator.getValue(ref);
				@SuppressWarnings("unchecked")
				Map<String,Double> daughterProducts = 
						(Map<String,Double>) compliant.getValue(ref);
				for ( String key : mumProducts.keySet() )
				{
					product = mumProducts.get(key);
					daughterProducts.put(key, product * (1.0-mumMassFrac) );
					mumProducts.put(key, product * mumMassFrac);
				}
				initiator.set(ref, mumProducts);
				compliant.set(ref, daughterProducts);
			}
		}
		if ( motherMass == null && product == null )
		{
			Log.out(Tier.CRITICAL, "Agent "+ ((Agent) initiator).identity()+
					" has an unrecognised mass type: "+
					mumMass.getClass().toString());
		}
	}
	
	public void updateAgents(Agent parent, Agent child)
	{
		if( child != null)
		{
			child.registerBirth();
			child.event(AspectRef.agentUpdateBody);
			/* Call the plasmid loss event 
			 * NOTE not sure wether this is the right place to do this, posibly move
			 * to it's own process manager? */
			if (child.isAspect(AspectRef.agentPlasmidLoss))
				child.event(AspectRef.agentPlasmidLoss);
			child.event(AspectRef.agentDivide);
		}
		/* if either is still larger than the division size they need to divide 
		 * again */
		parent.event(AspectRef.agentUpdateBody);
		parent.event(AspectRef.agentDivide);
	}
}
