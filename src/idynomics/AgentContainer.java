package idynomics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.Body;
import dataIO.XmlLabel;
import linearAlgebra.Vector;
import reaction.Reaction;
import shape.Shape;
import spatialRegistry.*;
import surface.BoundingBox;
import utility.ExtraMath;

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
	private SpatialRegistry<Agent> _agentTree;
	
	/**
	 * Synchronized list, list is cheaper to access than an agent tree
	 * (iterating over all agents), synchronized for thread safety
	 */
	List<Agent> _locatedAgentList = new ArrayList<Agent>();
	
	/**
	 * Synchronized iterator (check whether this wokrs correctly)
	 */
	Iterator<Agent> locatedAgentIterator;
	
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
	
	/**
	 * \brief TODO
	 * 
	 * @param xmlElem
	 */
	public void readAgents(NodeList agentNodes, Compartment comp)
	{
		for ( int i = 0; i < agentNodes.getLength(); i++ ) 
			this.addAgent(new Agent(agentNodes.item(i), comp));
	}
	
	public void setAllAgentsCompartment(Compartment aCompartment)
	{
		for ( Agent a : this._agentList )
			a.setCompartment(aCompartment);
		for ( Agent a : this._agentTree.all() )
			a.setCompartment(aCompartment);
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
		LinkedList<Agent> out = new LinkedList<Agent>();
		out.addAll(this._agentList);
		out.addAll(this._locatedAgentList);
		return out;
	}
	
	public List<Agent> treeSearch(BoundingBox boundingBox)
	{
		return _agentTree.cyclicsearch(boundingBox);
	}
	
	public List<Agent> treeSearch(List<BoundingBox> boundingBoxes)
	{
		return _agentTree.cyclicsearch(boundingBoxes);
	}
	
	public List<Agent> treeSearch(double[] location, double[] dimensions)
	{
		return _agentTree.cyclicsearch(location, dimensions);
	}
	
	public List<Agent> treeSearch(double[] pointLocation)
	{
		return treeSearch(pointLocation, Vector.zeros(pointLocation));
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
		if ( ( agent.get(NameRef.isLocated) == null ) || 
									( ! agent.getBoolean(NameRef.isLocated) ) )
		{
			this._agentList.add(agent);
		}
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
		_locatedAgentList.add(anAgent);
		treeInsert(anAgent);
	}
	
	/**
	 * NOTE the agent bounding box should encapsulate the entire influence
	 * region of the agent (thus also pull distance)
	 * @param anAgent
	 */
	protected void treeInsert(Agent anAgent)
	{
		for(BoundingBox b: ((Body) anAgent.get(NameRef.agentBody)).getBoxes(
				(anAgent.isAspect(NameRef.agentPulldistance) ?
				anAgent.getDouble(NameRef.agentPulldistance) :
				0.0)))
			this._agentTree.insert(b, anAgent);
	}
	
	public void refreshSpatialRegistry()
	{
		this.makeAgentTree();
		for(Agent a : getAllLocatedAgents())
		{
			this.treeInsert(a);
		}	
	}
	
	public List<Agent> getAllLocatedAgents()
	{
		List<Agent> out = new LinkedList<Agent>();
		out.addAll(_locatedAgentList);
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
	
	public Agent extractRandomAgent()
	{
		Agent out;
		int i = ExtraMath.getUniRandInt(this.getNumAllAgents());
		if ( i > this._agentList.size() )
		{
			// Located agent
			out = this._agentTree.getRandom();
			this._agentTree.delete(out);
		}
		else
		{
			// Unlocated agent
			out = this._agentList.remove(i);
		}
		return out;
	}
	
	/*************************************************************************
	 * NEIGHBOURHOOD GETTERS
	 ************************************************************************/
	
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	
}
