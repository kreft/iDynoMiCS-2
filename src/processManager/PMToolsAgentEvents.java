package processManager;

import agent.Agent;
import idynomics.AgentContainer;

public final class PMToolsAgentEvents
{

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
