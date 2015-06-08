package idynomics;

import java.util.LinkedList;

import spatialregistry.RTree;
import spatialregistry.spatialRegistry;
import agent.Agent;

public class AgentContainer
{
	/**
	 * all agents with a spatial location are stored in the RTree
	 */
	protected spatialRegistry<Agent> _agentTree;
	
	/**
	 * all agents without a spatial location are stored in the agent List
	 */
	protected LinkedList<Agent> _agentList = new LinkedList<Agent>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public AgentContainer()
	{

	}

	/**
	 * 
	 * @param nDims
	 * 			Number of dimensions in this domain (x,y,z)
	 */
	public void init(int nDims) 
	{
		// Bas - I have chosen maxEntries and minEntries by testing what values
		// resulted in fast tree creation and agent searches.
		_agentTree = new RTree<Agent>(8,2,nDims);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * Bas - I think the list should only be shuffled when needed or assumed
	 * needed since shuffling may become expansive with a high number of agents.
	 * @return A list of all Agents
	 */
	public LinkedList<Agent> getAllAgents()
	{
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(_agentList);
		out.addAll(_agentTree.all());
		return out;
	}

	public void registerBirth(Agent agent) {
		addAgent(agent);
	}

	private void addAgent(Agent agent) {
		if (agent.isLocated())
			_agentTree.insert(agent.getLower(),agent.getDim(),agent);
		else
			_agentList.add(agent);
		
	}
	
	
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
}
