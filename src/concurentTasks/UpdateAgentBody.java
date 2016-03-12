package concurentTasks;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.NameRef;

public class UpdateAgentBody implements ConcurrentTask
{

	private AgentContainer _agentContainer;

	public UpdateAgentBody(AgentContainer agents)
	{
		this._agentContainer = agents;
	}

	@Override
	public void task(int start, int end) {
		for(Agent agent: _agentContainer.getAllLocatedAgents().subList(start, end)) 
		{
			agent.event(NameRef.bodyUpdate);
			agent.event("divide");
			agent.event("epsExcretion");
		}
	}

	@Override
	public int size() {
		return _agentContainer.getAllLocatedAgents().size();
	}

}
