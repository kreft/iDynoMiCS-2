package idynomics;

import java.util.LinkedList;

import util.RTree;
import agent.Agent;

public class AgentContainer
{
	/**
	 * all agents with a spatial location are stored in the RTree
	 */
	protected RTree<Agent> agentTree;
	
	/**
	 * all agents without a spatial location are stored in the agent List
	 */
	protected LinkedList<Agent> agentList = new LinkedList<Agent>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public AgentContainer()
	{

	}

	public void init(int nDims) 
	{
		agentTree = new RTree<Agent>(8,2,nDims);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public LinkedList<Agent> getAllAgents()
	{
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(agentList);
		out.addAll(agentTree.all());
		return out;
	}
	
	
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
}
