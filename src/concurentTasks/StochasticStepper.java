package concurentTasks;

import java.util.List;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import surface.Point;

public class StochasticStepper implements ConcurrentTask
{
	private List<Agent> agentList;
	private double dtMech;

	public StochasticStepper(AgentContainer agents, double dt)
	{
		this.dtMech = dt;
		agentList = agents.getAllLocatedAgents();
	}
	
	public StochasticStepper(List<Agent> agentList, double dt)
	{
		this.agentList = agentList;
	}
	

	@Override
	public ConcurrentTask part(int start, int end) 
	{
		return new StochasticStepper(agentList.subList(start, end), dtMech);
	}

	public void task() {
		// Calculate forces
		for(Agent agent: agentList) 
		{
			if (agent.isAspect("stochasticStep"))
				agent.event("stochasticMove", dtMech);
		}
	}

	public int size() 
	{
		return agentList.size();
	}
}