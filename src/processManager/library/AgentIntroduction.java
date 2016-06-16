/**
 * 
 */
package processManager.library;

import java.util.ArrayList;
import java.util.List;

import agent.Agent;
import processManager.ProcessManager;

/**
 * \brief Process that introduces new agents to the compartment.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class AgentIntroduction extends ProcessManager
{
	/**
	 * List of agents we want to introduce.
	 */
	protected List<Agent> _agentsToIntroduce = new ArrayList<Agent>();
	
	// TODO reading in agents
	
	@Override
	protected void internalStep()
	{
		/*
		 * Put all the agents into the compartment.
		 */
		for ( Agent agent : this._agentsToIntroduce )
			this._agents.addAgent(agent);
		/*
		 * Remove them from our list.
		 */
		// TODO make a copy of the agents, instead of removing them?
		this._agentsToIntroduce.clear();
	}

}
