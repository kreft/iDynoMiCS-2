package concurentTasks;

import java.util.List;

import agent.Agent;
import idynomics.AgentContainer;

public class UpdateAgentBody implements ConcurrentTask
{

	private List<Agent> agentList;

	public UpdateAgentBody(AgentContainer agents)
	{
		this.agentList = agents.getAllLocatedAgents();
	}
	
	public UpdateAgentBody(List<Agent> cList)
	{
		agentList = cList;
	}

	public ConcurrentTask part(int start, int end)
	{
		return new UpdateAgentBody(agentList.subList(start,end));
	}

	public void task() 
	{
		for(Agent agent: agentList) 
		{
			agent.event("updateBody");
			agent.event("divide");
			agent.event("epsExcretion");
		}
	}

	public int size() 
	{
		return agentList.size();
	}

}
