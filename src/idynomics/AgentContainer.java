package idynomics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import reaction.Reaction;
import spatialRegistry.*;

public class AgentContainer
{
	protected int nDim;
	/**
	 * All agents with a spatial location are stored in the agentTree 
	 * (e.g. an RTree).
	 */
	public SpatialRegistry<Agent> _agentTree;
	
	/**
	 * All agents without a spatial location are stored in the agentList.
	 */
	protected LinkedList<Agent> _agentList = new LinkedList<Agent>();
	
	/**
	 * All reactions performed by agents.
	 * TODO Check this is the best way of going about things!
	 */
	protected HashMap<String, Reaction> _agentReactions;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * 
	 */
	public AgentContainer()
	{
		
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param nDims	Number of dimensions in this domain (x,y,z).
	 */
	public void init(int compartmentNumDims) 
	{
		this.nDim = compartmentNumDims;
		/*
		 * Bas: I have chosen maxEntries and minEntries by testing what values
		 * resulted in fast tree creation and agent searches.
		 */
		if ( nDim == 0 )
			this._agentTree = new DummyTree<Agent>();
		else
			this._agentTree = new RTree<Agent>(8, 2, this.nDim);
		
		/*
		 * No parameters needed for the agentList.
		 */
		this._agentList = new LinkedList<Agent>();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public int getNumDims()
	{
		return nDim;
	}
	
	/**
	 * \brief 
	 * 
	 * Bas: I think the list should only be shuffled when needed or assumed
	 * needed since shuffling may become expansive with a high number of
	 * agents.
	 * 
	 * @return A list of all Agents, i.e. those with spatial location AND
	 * those without.
	 */
	public LinkedList<Agent> getAllAgents()
	{
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(this._agentList);
		out.addAll(this._agentTree.all());
		return out;
	}
	
	/*
	 * Legacy support =)
	 */
	public void registerBirth(Agent agent) {
		addAgent(agent);
	}

	//FIXME: .isLocated simplified for now, was an over extensive operation for a simple check.
	public void addAgent(Agent agent)
	{
		if ( (boolean) agent.get("isLocated") )
			this._agentTree.insert((float[]) agent.get("lowerBoundingBox"), (float[]) agent.get("dimensionsBoundingBox"), agent);
		else
			this._agentList.add(agent);
		
	}
	
	public void refreshSpatialRegistry()
	{
		List<Agent> agentList = _agentTree.all();
		this._agentTree = new RTree<Agent>(8, 2, this.nDim);
		for(Agent a: agentList) 
			_agentTree.insert((float[]) a.get("lowerBoundingBox"), (float[]) a.get("dimensionsBoundingBox"), a);
	}
	
	public LinkedList<Agent> getAllLocatedAgents()
	{
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(_agentTree.all());
		return out;
	}
	
	public LinkedList<Agent> getAllUnlocatedAgents()
	{
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(_agentList);
		return out;
	}
	
	public int getNumAllAgents()
	{
		return this._agentList.size() + this._agentTree.all().size();
	}
	
	/*************************************************************************
	 * NEIGHBOURHOOD GETTERS
	 ************************************************************************/
	
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	
}
