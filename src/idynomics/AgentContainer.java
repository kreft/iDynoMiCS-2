package idynomics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.NodeList;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import boundary.Boundary;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import static dataIO.Log.Tier.*;
import linearAlgebra.Vector;
import shape.Dimension;
import shape.Shape;
import shape.Dimension.DimName;
import spatialRegistry.*;
import surface.BoundingBox;
import surface.Collision;
import surface.Surface;
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
	private List<Agent> _locatedAgentList = new ArrayList<Agent>();
	
	/**
	 * All agents without a spatial location are stored in here.
	 */
	protected LinkedList<Agent> _agentList = new LinkedList<Agent>();
	
	/**
	 * All dead agents waiting for their death to be recorded as output before
	 * they can be removed from memory.
	 */
	protected List<Agent> _agentsToRegisterRemoved = new LinkedList<Agent>();
	
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
		return this._shape.getNumberOfDimensions();
	}
	
	public Shape getShape()
	{
		return this._shape;
	}

	/**
	 * @return A count of all {@code Agent}s, including both located and
	 * non-located.
	 */
	public int getNumAllAgents()
	{
		return this._agentList.size() + this._locatedAgentList.size();
	}
	
	
	/**
	 * @return A list of all {@code Agent}s which have a location.
	 */
	public List<Agent> getAllLocatedAgents()
	{
		List<Agent> out = new LinkedList<Agent>();
		out.addAll(this._locatedAgentList);
		return out;
	}
	
	/**
	 * @return A list of all {@code Agent}s which do not have a location.
	 */
	public List<Agent> getAllUnlocatedAgents()
	{
		List<Agent> out = new LinkedList<Agent>();
		out.addAll(this._agentList);
		return out;
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
	
	/*************************************************************************
	 * LOCATED SEARCHES
	 ************************************************************************/
	
	public List<Agent> treeSearch(BoundingBox boundingBox)
	{
		return this._agentTree.cyclicsearch(boundingBox);
	}
	
	public List<Agent> treeSearch(List<BoundingBox> boundingBoxes)
	{
		return this._agentTree.cyclicsearch(boundingBoxes);
	}
	
	public List<Agent> treeSearch(double[] location, double[] dimensions)
	{
		return this._agentTree.cyclicsearch(location, dimensions);
	}
	
	public List<Agent> treeSearch(double[] pointLocation)
	{
		return this.treeSearch(pointLocation, Vector.zeros(pointLocation));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 * @param searchDist
	 * @return
	 */
	public Collection<Agent> treeSearch(Agent anAgent, double searchDist)
	{
		// TODO not sure if this is the best response
		if ( ! isLocated(anAgent) )
			return new LinkedList<Agent>();
		/*
		 * Find all nearby agents.
		 */
		Body body = (Body) anAgent.get(AspectRef.agentBody);
		List<BoundingBox> boxes = body.getBoxes(searchDist);
		Collection<Agent> out = this.treeSearch(boxes);
		/* 
		 * Remove the focal agent from this list.
		 */
		out.removeIf((a) -> {return a == anAgent;});
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 * @param searchDist
	 * @return
	 */
	public Collection<Surface> surfaceSearch(Agent anAgent, double searchDist)
	{
		Collection<Surface> out = this._shape.getSurfaces();
		Collision collision = new Collision(this._shape);
		Collection<Surface> agentSurfs = 
				((Body) anAgent.get(AspectRef.agentBody)).getSurfaces();
		out.removeIf((s) -> 
		{
			for (Surface a : agentSurfs )
				if ( collision.distance(a, s) < searchDist )
					return false;
			return true;
		});
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 * @param searchDist
	 * @return
	 */
	public Collection<SpatialBoundary> boundarySearch(Agent anAgent, double searchDist)
	{
		Collection<SpatialBoundary> out = new LinkedList<SpatialBoundary>();
		for ( Surface s : this.surfaceSearch(anAgent, searchDist) )
			out.add(this._shape.getSurfaceBounds().get(s));
		return out;
	}
	
	/*************************************************************************
	 * AGENT LOCATION
	 ************************************************************************/

	/**
	 * \brief Helper method to check if an {@code Agent} is located.
	 * 
	 * @param anAgent {@code Agent} to check.
	 * @return Whether it is located (true) or not located (false).
	 */
	public static boolean isLocated(Agent anAgent)
	{
		/*
		 * If there is no flag saying this agent is located, assume it is not.
		 * 
		 * Note of terminology: this is known as the closed-world assumption.
		 * https://en.wikipedia.org/wiki/Closed-world_assumption
		 */
		//FIXME: #isLocated simplified for now, was an over extensive operation
		// for a simple check.
		return ( anAgent.get(AspectRef.isLocated) != null ) && 
				( anAgent.getBoolean(AspectRef.isLocated) );
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 * @param dimN
	 * @param dist
	 */
	public void moveAlongDimension(Agent anAgent, DimName dimN, double dist)
	{
		if ( ! isLocated(anAgent) )
			return;
		Body body = (Body) anAgent.get(AspectRef.agentBody);
		double[] newLoc = body.getPoints().get(0).getPosition();
		this._shape.moveAlongDimension(newLoc, dimN, dist);
		body.relocate(newLoc);
		Log.out(DEBUG, "Moving agent (UID: "+anAgent.identity()+
				") along dimension "+dimN+" to "+Vector.toString(newLoc));
	}
	
	/*************************************************************************
	 * ADDING & REMOVING AGENTS
	 ************************************************************************/
	
	/**
	 * \brief Add the given <b>agent</b> to the appropriate list.
	 * 
	 * @param agent {@code Agent} object to be accepted into this
	 * {@code AgentContainer}.
	 */
	public void addAgent(Agent agent)
	{
		if ( isLocated(agent) )
			this.addLocatedAgent(agent);
		else
			this._agentList.add(agent);
	}
	
	/**
	 * \brief Add an {@code Agent} that is known to be located into the agent
	 * tree.
	 * 
	 * @param anAgent {@code Agent} object that you are sure is located!
	 */
	protected void addLocatedAgent(Agent anAgent)
	{
		this._locatedAgentList.add(anAgent);
		this.treeInsert(anAgent);
	}
	
	/**
	 * NOTE the agent bounding box should encapsulate the entire influence
	 * region of the agent (thus also pull distance)
	 * @param anAgent
	 */
	protected void treeInsert(Agent anAgent)
	{
		Body body = ((Body) anAgent.get(AspectRef.agentBody));
		double dist = (anAgent.isAspect(AspectRef.agentPulldistance) ?
				anAgent.getDouble(AspectRef.agentPulldistance) :
				0.0);
		List<BoundingBox> boxes = body.getBoxes(dist);
		for ( BoundingBox b: boxes )
			this._agentTree.insert(b, anAgent);
	}
	
	/**
	 * \brief Rebuild the spatial registry, by removing and then re-inserting 
	 * all located agents.
	 */
	public void refreshSpatialRegistry()
	{
		this.makeAgentTree();
		for ( Agent a : this.getAllLocatedAgents() )
			this.treeInsert(a);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param i
	 * @return
	 */
	public Agent chooseAgent(int i)
	{
		return (i > this._agentList.size()) ?
				/* Located agent. */
				this._locatedAgentList.get(i - this._agentList.size()) :
				/* Unlocated agent. */
				this._agentList.get(i);
	}
	
	/**
	 * @return A random {@code Agent}, without removing it from this container.
	 * @see #extractRandomAgent()
	 */
	public Agent chooseRandomAgent()
	{
		Tier level = Tier.BULK;
		int nAgents = this.getNumAllAgents();
		/* Safety if there are no agents. */
		if ( nAgents == 0 )
		{
			Log.out(level, "No agents in this container, so cannot choose "+
					"one: returning null");
			return null;
		}/* Now find an agent. */
		int i = ExtraMath.getUniRandInt(nAgents);
		Agent out = this.chooseAgent(i);
		Log.out(level, "Out of "+nAgents+" agents, agent with UID "+
				out.identity()+" was chosen randomly");
		return out; 
	}
	
	/**
	 * @return A randomly chosen {@code Agent}, who is removed from this
	 * container.
	 * @see #chooseRandomAgent()
	 */
	public Agent extractRandomAgent()
	{
		Tier level = Tier.BULK;
		int nAgents = this.getNumAllAgents();
		/* Safety if there are no agents. */
		if ( nAgents == 0 )
		{
			Log.out(level, "No agents in this container, so cannot extract "+
					"one: returning null");
			return null;
		}/* Now find an agent. */
		int i = ExtraMath.getUniRandInt(nAgents);
		Agent out;
		if ( i > this._agentList.size() )
		{
			/* Located agent. */
			out = this._locatedAgentList.remove(i - this._agentList.size());
			this._agentTree.delete(out);
		}
		else
		{
			/* Unlocated agent. */
			out = this._agentList.remove(i);
		}
		Log.out(level, "Out of "+nAgents+" agents, agent with UID "+
				out.identity()+" was extracted randomly");
		return out; 
	}
	
	/**
	 * \brief Move the given agent into the list to report and remove.
	 * 
	 * @param anAgent Agent to remove.
	 */
	// TODO unify method for removing a located agent? See extractRandomAgent
	// above
	public void registerRemoveAgent(Agent anAgent)
	{
		if ( isLocated(anAgent) )
		{
			this._locatedAgentList.remove(anAgent);
			this._agentTree.delete(anAgent);
		}
		else
			this._agentList.remove(anAgent);
		this._agentsToRegisterRemoved.add(anAgent);
	}
	
	/**
	 * \brief Loop over all boundaries, asking any agents waiting in their
	 * arrivals lounges to enter the compartment.
	 */
	 
	public void agentsArrive()
	{
		Tier level = BULK;
		Log.out(level, "Agents arriving into compartment...");
		Dimension dim;
		for ( DimName dimN : this._shape.getDimensionNames() )
		{
			dim = this._shape.getDimension(dimN);
			if ( dim.isCyclic() )
			{
				Log.out(level, "   "+dimN+" is cyclic, skipping");
				continue;
			}
			if ( ! dim.isSignificant() )
			{
				Log.out(level, "   "+dimN+" is insignificant, skipping");
				continue;
			}
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				Log.out(level, "Looking at "+dimN+" "+((extreme==0)?"min":"max"));
				if ( ! dim.isBoundaryDefined(extreme) )
				{
					Log.out(level, "   boundary not defined");
					continue;
				}
				dim.getBoundary(extreme).agentsArrive(this);
				Log.out(level, "   boundary defined, agents ariving");
			}
		}
		for ( Boundary bndry : this._shape.getOtherBoundaries() )
		{
			Log.out(level,"   other boundary "+bndry.getName()+
					", calling agent method");
			bndry.agentsArrive(this);
		}
		Log.out(level, " All agents have now arrived");
	}
	
	/**
	 * \brief Loop over all boundaries, asking any agents waiting in their
	 * departure lounges to leave the compartment.
	 */
	
	public void agentsDepart()
	{
		Tier level = BULK;
		Log.out(level, "Pushing all outbound agents...");
		Dimension dim;
		for ( DimName dimN : this._shape.getDimensionNames() )
		{
			dim = this._shape.getDimension(dimN);
			if ( dim.isCyclic() )
			{
				Log.out(level, "   "+dimN+" is cyclic, skipping");
				continue;
			}
			if ( ! dim.isSignificant() )
			{
				Log.out(level, "   "+dimN+" is insignificant, skipping");
				continue;
			}
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				Log.out(level, 
						"Looking at "+dimN+" "+((extreme==0)?"min":"max"));
				if ( ! dim.isBoundaryDefined(extreme) )
				{
					Log.out(level, "   boundary not defined");
					continue;
				}
				Log.out(level, "   boundary defined, pushing agents");
				dim.getBoundary(extreme).pushAllOutboundAgents();
			}
		}
		for ( Boundary bndry : this._shape.getOtherBoundaries() )
		{
			Log.out(level,"   other boundary "+bndry.getName()+
					", pushing agents");
			bndry.pushAllOutboundAgents();
		}
		Log.out(level, " All agents have now departed");
	}
	
	/**
	 * @return {@code true} is this has any agents to report and destroy.
	 */
	public boolean hasAgentsToRegisterRemoved()
	{
		return ( ! this._agentsToRegisterRemoved.isEmpty());
	}
	
	/**
	 * \brief Report all agents registered for removal as an XML string for
	 * output, then delete these agents from memory.
	 * 
	 * @return String report of accumulated agents to remove.
	 */
	public String reportDestroyRemovedAgents()
	{
		String out = "";
		// FIXME this gap needs filling
		//for ( Agent anAgent : this._deadAgents )
		//	out += anAgent.getXML();
		this._agentsToRegisterRemoved.clear();
		return out;
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/

}
