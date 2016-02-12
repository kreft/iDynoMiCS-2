package idynomics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import reaction.Reaction;
import shape.Shape;
import spatialRegistry.*;

/**
 * \brief Manages the agents in a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentContainer
{
	/**
	 * This dictates both geometry and size, and it inherited from the
	 * {@code Compartment} this {@code AgentContainer} belongs to.
	 */
	protected Shape _shape;
	/**
	 * All agents with a spatial location are stored in here (e.g. an RTree).
	 */
	public SpatialRegistry<Agent> _agentTree;
	/**
	 * All agents without a spatial location are stored in here.
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
	 * \brief Construct an {@code AgentContainer} from a {@code Shape}.
	 * 
	 * @param aShape {@code Shape} object (see shape.ShapeLibrary).
	 */
	public AgentContainer(Shape aShape)
	{
		this._shape = aShape;
		this.makeAgentTree();
		this._agentList = new LinkedList<Agent>();
	}
	
	/**
	 * \brief Construct an {@code AgentContainer} from the name of a
	 * {@code Shape}.
	 * 
	 * <p>Used by test classes.</p>
	 * 
	 * @param shapeName {@code String} name of a shape.
	 */
	public AgentContainer(String shapeName)
	{
		this((Shape) Shape.getNewInstance(shapeName));
	}
	
	protected void makeAgentTree()
	{
		/*
		 * Bas: I have chosen maxEntries and minEntries by testing what values
		 * resulted in fast tree creation and agent searches.
		 * 
		 * TODO rtree paramaters could follow from the protocol file.
		 */
		if ( this._shape.getNumberOfDimensions() == 0 )
			this._agentTree = new DummyTree<Agent>();
		else
			this._agentTree = new RTree<Agent>(8, 2, this._shape);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public int getNumDims()
	{
		return _shape.getNumberOfDimensions();
	}
	
	public Shape getShape()
	{
		return _shape;
	}
	
	/**
	 * \brief Get a list of all {@code Agent}s.
	 * 
	 * @return A list of all Agents, i.e. those with spatial location AND
	 * those without.
	 */
	public LinkedList<Agent> getAllAgents()
	{
		/*
		 * Bas: I think the list should only be shuffled when needed or assumed
		 * needed since shuffling may become expensive with a high number of
	 	 * agents.
	 	 * 
	 	 * Rob [3Feb2016]: That's fine with me.
		 */
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(this._agentList);
		out.addAll(this._agentTree.all());
		return out;
	}
	
	/**
	 * \brief Add the given <b>agent</b> to the appropriate list.
	 * 
	 * @param agent {@code Agent} object to be accepted into this
	 * {@code AgentContainer}.
	 */
	public void addAgent(Agent agent)
	{
		//FIXME: #isLocated simplified for now, was an over extensive operation
		// for a simple check.
		if (agent.get("#isLocated") == null || !((boolean) agent.get("#isLocated")) )
			this._agentList.add(agent);
		else
			this.addLocatedAgent(agent);
		
	}
	
	/**
	 * \brief Add an {@code Agent} that is known to be located into the agent
	 * tree.
	 * 
	 * @param anAgent {@code Agent} object that you are sure is located!
	 */
	protected void addLocatedAgent(Agent anAgent)
	{
		this._agentTree.insert(
							(double[]) anAgent.get("#boundingLower"),
							(double[]) anAgent.get("#boundingSides"), anAgent);
	}
	
	public void refreshSpatialRegistry()
	{
		List<Agent> agentList = this._agentTree.all();
		this.makeAgentTree();
		for ( Agent anAgent : agentList )
			this.addLocatedAgent(anAgent);
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
