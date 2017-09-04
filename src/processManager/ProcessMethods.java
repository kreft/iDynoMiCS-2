package processManager;

import java.util.HashMap;
import java.util.Map;

import agent.Agent;
import referenceLibrary.AspectRef;

/**
 * Process related methods moved out of general classes. Single agent related
 * steps may be considered to be reformed into agent event or calculated aspect.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ProcessMethods {

	/**
	 * \brief Use a dictionary of biomass names and values to update the given
	 * agent.
	 * 
	 * <p>This method is the opposite of {@link ProcessMethods#getAgentMassMap(Agent)}. Note
	 * that extra biomass types may have been added to the map, which should
	 * be other aspects (e.g. EPS).</p>
	 * 
	 * @param agent An agent with biomass.
	 * @param biomass Dictionary of biomass kind names to their values.
	 */
	public static void updateAgentMass(Agent agent, Map<String,Double> biomass)
	{
		/*
		 * First try to copy the new values over to the agent mass aspect.
		 * Remember to remove the key-value pairs from biomass, so that we can
		 * see what is left (if anything).
		 */
		Object mass = agent.get(AspectRef.agentMass);
		if ( mass == null )
		{
			// TODO safety?
		}
		else if ( mass instanceof Double )
		{
			/**
			 * NOTE map.remove returns the current associated value and removes
			 * it from the map
			 */
			agent.set(AspectRef.agentMass, biomass.remove(AspectRef.agentMass));
		}
		else if ( mass instanceof Map )
		{
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) mass;
			for ( String key : massMap.keySet() )
			{
				massMap.put(key, biomass.remove(key));
			}
			
			agent.set(AspectRef.agentMass, biomass);
		}
		else
		{
			// TODO safety?
		}
		/*
		 * Now check if any other aspects were added to biomass (e.g. EPS).
		 */
		for ( String key : biomass.keySet() )
		{
			if ( agent.isAspect(key) )
			{
				agent.set(key, biomass.get(key));
				biomass.remove(key);
			}
			else
			{
				// TODO safety
			}
		}
	}

	/**
	 * \brief Compose a dictionary of biomass names and values for the given
	 * agent.
	 * 
	 * <p>this method is the opposite of 
	 * {@link #updateAgentMass(Agent, HashMap<String,Double>)}.</p>
	 * 
	 * @param agent An agent with biomass.
	 * @return Dictionary of biomass kind names to their values.
	 */
	// TODO move this, and updateAgentMass(), to somewhere more general?
	public static Map<String,Double> getAgentMassMap(Agent agent)
	{
		Map<String,Double> out = new HashMap<String,Double>();
		Object mass = agent.get(AspectRef.agentMass);
		if ( mass == null )
		{
			// TODO safety?
		}
		else if ( mass instanceof Double )
		{
			out.put(AspectRef.agentMass, ((double) mass));
		}
		else if ( mass instanceof Map )
		{
			/* If the mass object is already a map, then just copy it. */
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) mass;
			out.putAll(massMap);
		}
		else
		{
			// TODO safety?
		}
		return out;
	}

}
