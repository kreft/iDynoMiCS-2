package agent.predicate;

import java.util.function.Predicate;

import agent.Agent;

public class HasAspect implements Predicate<Agent> {

	private String _aspectName;

	public HasAspect(String aspectName)
	{
		this._aspectName = aspectName;
	}
	
	@Override
	public boolean test(Agent agent)
	{
		return agent.isAspect(_aspectName);
	}
	
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return "Agent aspect " + _aspectName;
	}
}