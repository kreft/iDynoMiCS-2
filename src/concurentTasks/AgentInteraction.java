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
	
	public ConcurrentTask part(int start, int end) 
	{
		return new AgentInteraction(agentList.subList(start, end), 
				_agentContainer);
	}

	public void task() {
		for(Agent agent: agentList) 
		{
			
			/**
			 * NOTE: currently missing internal springs for rod cells.
			 */
			double searchDist = (agent.isAspect("searchDist") ?
					agent.getDouble("searchDist") : 0.0);
			
			/**
			 * perform neighborhood search and perform collision detection and
			 * response FIXME: this has not been adapted to multi surface
			 * objects!
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
					
					iterator.collision((Surface) agent.get("surface"), 
							(Surface) neighbour.get("surface"), pull);
				}
			}
			
			/*
			 * Boundary collisions
			 */
			for(Surface s : _agentContainer.getShape().getSurfaces())
			{
				iterator.collision(s, (Surface) agent.get("surface"), 0.0);
			}
		}
	}

	public int size() 
	{
		return agentList.size();
	}


}
