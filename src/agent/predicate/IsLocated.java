package agent.predicate;

import java.util.function.Predicate;

import agent.Agent;
import referenceLibrary.AspectRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class IsLocated  implements Predicate<Agent> {

	@Override
	public boolean test(Agent agent)
	{
		return isLocated(agent);
	}
	
	/**
	 * \brief Helper method to check if an {@code Agent} is located.
	 * 
	 * If there is no flag saying this agent is located, assume it is not.
	 * 
	 * Note of terminology: this is known as the closed-world assumption.
	 * https://en.wikipedia.org/wiki/Closed-world_assumption
	 * 
	 * @param agent {@code Agent} to check.
	 * @return Whether it is located (true) or not located (false).
	 */
	public static boolean isLocated(Agent agent)
	{
		return ( agent.get(AspectRef.agentBody) != null );
//				( agent.getBoolean(AspectRef.isLocated) );
	}
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return "Agent is located.";
	}
}