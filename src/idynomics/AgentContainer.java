package idynomics;

import java.util.LinkedList;

import agent.Agent;
import spatialRegistry.*;

public class AgentContainer
{
	/**
	 * All agents with a spatial location are stored in the agentTree 
	 * (e.g. an RTree).
	 */
	protected SpatialRegistry<Agent> _agentTree;
	
	/**
	 * All agents without a spatial location are stored in the agentList.
	 */
	protected LinkedList<Agent> _agentList = new LinkedList<Agent>();
	
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
	public void init(int nDims) 
	{
		/*
		 * Bas: I have chosen maxEntries and minEntries by testing what values
		 * resulted in fast tree creation and agent searches.
		 */
		_agentTree = new RTree<Agent>(8, 2, nDims);
		/*
		 * No parameters needed for the agentList.
		 */
		_agentList = new LinkedList<Agent>();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
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
		out.addAll(_agentList);
		out.addAll(_agentTree.all());
		return out;
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
	
	/*************************************************************************
	 * NEIGHBOURHOOD GETTERS
	 ************************************************************************/
	
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	
}
