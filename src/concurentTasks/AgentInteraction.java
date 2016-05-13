package concurentTasks;

import java.util.List;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import idynomics.NameRef;
import surface.Collision;
import surface.Surface;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class AgentInteraction  implements ConcurrentTask
{

	private AgentContainer _agentContainer;
	private Collision iterator;
	private List<Agent> agentList;

	public AgentInteraction(AgentContainer agents)
	{
		this._agentContainer = agents;
		agentList = _agentContainer.getAllLocatedAgents();
		iterator = new Collision(null, agents.getShape());
	}
	
	public AgentInteraction(List<Agent> agentList, AgentContainer agents)
	{
		this._agentContainer = agents;
		this.agentList = agentList;
		iterator = new Collision(null, agents.getShape());
	}
	

	@Override
	public ConcurrentTask part(int start, int end) {
		return new AgentInteraction(agentList.subList(start, end), _agentContainer);
	}

	public void task() {
		// Calculate forces
		for(Agent agent: agentList) 
		{
			
			/**
			 * NOTE: currently missing internal springs for rod cells.
			 */
			
			double searchDist = (agent.isAspect("searchDist") ?
					agent.getDouble("searchDist") : 0.0);
			
			/**
			 * perform neighborhood search and perform collision detection and
			 * response 
			 */
			for(Agent neighbour: _agentContainer.treeSearch(

					((Body) agent.get(NameRef.agentBody)).getBoxes(
							searchDist)))
			{
				if (agent.identity() > neighbour.identity())
				{
					
					agent.event("evaluatePull", neighbour);
					Double pull = agent.getDouble("#curPullDist");
					
					if (pull == null || pull.isNaN())
						pull = 0.0;
					
					for (Surface s : ((Body) agent.get("body")).getSurfaces())
					{
						for (Surface t : ((Body) neighbour.get("body")).getSurfaces())
						{
							iterator.collision(s, t, pull);
						}
					}
				}
			}
			
			/*
			 * Boundary collisions
			 */
			for(Surface s : _agentContainer.getShape().getSurfaces())
			{
				for (Surface t : ((Body) agent.get("body")).getSurfaces())
				{
					iterator.collision(s, t, 0.0);
				}
			}
		}
	}

	public int size() {
		return agentList.size();
	}


}
