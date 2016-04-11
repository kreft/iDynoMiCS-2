package processManager;

import agent.Agent;
import idynomics.AgentContainer;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class PMToolsAgentEvents
{
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 * @param dt
	 */
	static void agentsGrow(AgentContainer agents, double dt)
	{
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			// TODO these strings are important, so should probably be in
			// XmlLabel or NameRef. What is "produce"?
			a.event("growth", dt);
			a.event("produce", dt);
		}
	}

}
