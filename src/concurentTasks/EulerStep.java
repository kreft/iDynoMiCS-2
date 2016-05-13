package concurentTasks;

import java.util.List;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import surface.Point;

public class EulerStep implements ConcurrentTask
{
	private List<Agent> agentList;
	private double dtMech;

	public EulerStep(AgentContainer agents, double dt)
	{
		this.dtMech = dt;
		agentList = agents.getAllLocatedAgents();
	}
	
	public EulerStep(List<Agent> agentList, double dt)
	{
		this.agentList = agentList;
	}
	

	@Override
	public ConcurrentTask part(int start, int end) {
		return new EulerStep(agentList.subList(start, end), dtMech);
	}

	public void task() {
		// Calculate forces
		for(Agent agent: agentList) 
		{
			for (Point point: ((Body) agent.get("body")).getPoints())
				point.euStep(dtMech, (double) agent.get("radius"));
		}
	}

	public int size() 
	{
		return agentList.size();
	}

}
