package compartment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import agent.Agent;
import agent.Body;
import agent.predicate.IsLocated;
import boundary.Boundary;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import debugTools.SegmentTimer;
import gereralPredicates.IsSame;

import static dataIO.Log.Tier.*;
import linearAlgebra.Vector;
import physicalObject.PhysicalObject;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Dimension;
import shape.Shape;
import shape.Dimension.DimName;
import spatialRegistry.*;
import spatialRegistry.splitTree.SplitTree;
import surface.BoundingBox;
import surface.predicate.IsNotColliding;
import surface.Surface;
import surface.collision.Collision;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Manages the agents in a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class AgentContainer implements Settable
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
	/**
	 * Physical Objects
	 */
	protected LinkedList<PhysicalObject> _physicalObjects = 
											new LinkedList<PhysicalObject>();
	/**
	 * Parent node (required for settable interface)
	 */
	private Settable _parentNode;
	
	/* NOTE removed predicates, use agent.predicate.HasAspect instad.

	/**
	 * the type of spatial registry ( setting default value but can be 
	 * overwritten).
	 */
	private TreeType _spatialTreeType = TreeType.SPLITTREE;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/

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
	 * \brief set the spatial tree type used for neighborhood searches
	 * 
	 * @param type
	 */
	public void setSpatialTreeType(TreeType type) 
	{
		this._spatialTreeType = type;
	}
	
	/**
	 * \brief Get the TreeType of the spatial tree used by this AgentContainer
	 * @return
	 */
	public TreeType getSpatialTreeType() 
	{
		return this._spatialTreeType;
	}
	
	/**
	 * TODO get spatial registry paradigm (like get shape)
	 */

	/**
	 * Helper method for (re-)making this container's spatial registry.
	 */
	protected void makeAgentTree()
	{
		if ( this.getNumDims() == 0 )
			/* FIXME is this required? nothing should be stored in a tree if the
			 * compartment is not spatial explicit */
			this._agentTree = new DummyTree<Agent>();
		else
		{
			switch (_spatialTreeType)
			{
			case RTREE:
				this._agentTree = new RTree<Agent>(8, 2, this._shape);
				break;
			case SPLITTREE:
				/* currently domain minimum is always set to zero but this will
				 * change, min represents domain minima */
				double[] min = Vector.zerosDbl(
						this.getShape().getNumberOfDimensions() );
				/* The 2D optimum could be different from 3D.  */
				this._agentTree = new SplitTree<Agent>(9, 
						min, Vector.add( min, 
						this.getShape().getDimensionLengths() ),
						this._shape.getIsCyclicNaturalOrder() );
				break;
			}
		}
	}

	/**
	 * \brief Helper method setting the compartment reference of every agent
	 * in this container to that given.
	 * 
	 * @param aCompartment {@code Compartment} object to give to every agent.
	 */
	public void setAllAgentsCompartment(Compartment aCompartment)
	{
		for ( Agent a : this._agentList )
			a.setCompartment(aCompartment);
		for ( Agent a : this._locatedAgentList )
			a.setCompartment(aCompartment);
	}

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	/**
	 * @return The number of "true" dimensions in the compartment.
	 */
	public int getNumDims()
	{
		return this._shape.getNumberOfDimensions();
	}

	/**
	 * @return The shape of this compartment.
	 */
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
		return this._locatedAgentList;
	}
	
	public List<Agent> getAllLocatedAgentsSafety()
	{
		ArrayList<Agent> out = 
				new ArrayList<Agent>(this._locatedAgentList.size() );
		out.addAll( this._locatedAgentList );
		return out;
	}

	/**
	 * @return A list of all {@code Agent}s which do not have a location.
	 */
	public List<Agent> getAllUnlocatedAgents()
	{
		return this._agentList;
	}

	/**
	 * \brief Get a list of all {@code Agent}s.
	 * 
	 * @return A list of all Agents, i.e. those with spatial location AND
	 * those without.
	 */
	public List<Agent> getAllAgents()
	{
		ArrayList<Agent> out = new ArrayList<Agent>(
				this._agentList.size()+this._locatedAgentList.size() );
		out.addAll(this._agentList);
		out.addAll(this._locatedAgentList);
		return out;
	}
	
	public LinkedList<PhysicalObject> getAllPhysicalObjects()
	{
		LinkedList<PhysicalObject> out = new LinkedList<PhysicalObject>();
		out.addAll(this._physicalObjects);
		return out;
	}

	/* ***********************************************************************
	 * LOCATED SEARCHES
	 * **********************************************************************/

	/**
	 * \brief Find agents that may overlap with the given bounding box.
	 * 
	 * @param boundingBox The {@code BoundingBox} object to search in.
	 * @return Collection of agents that may be overlap with this box: note
	 * that there may be some false positives (but no false negatives).
	 */
	public List<Agent> treeSearch(BoundingBox boundingBox)
	{
		return this._agentTree.search(boundingBox);
	}

	/**
	 * \brief Find agents that may overlap with at least one of the given
	 * bounding boxes.
	 * 
	 * @param boundingBoxes The {@code BoundingBox} objects to search in.
	 * @return Collection of agents that may be overlap with these boxes: note
	 * that there may be some false positives (but no false negatives).
	 */
	public List<Agent> treeSearch(List<BoundingBox> boundingBoxes)
	{
		return this._agentTree.search(boundingBoxes);
	}

	/**
	 * \brief Find agents that may overlap with the given search box.
	 * 
	 * @param location Vector representing the origin of this search box.
	 * @param dimensions Vector representing the size of this search box in
	 * each dimension.
	 * @return Collection of agents that may be overlap with this box: note
	 * that there may be some false positives (but no false negatives).
	 */
	public List<Agent> treeSearch(double[] location, double[] dimensions)
	{
		return this._agentTree.search(location, dimensions);
	}

	/**
	 * \brief Find agents that may overlap with the given point location.
	 * 
	 * @param pointLocation Vector representing a point in space.
	 * @return Collection of agents that may be overlap with this point: note
	 * that there may be some false positives (but no false negatives).
	 */
	public List<Agent> treeSearch(double[] pointLocation)
	{
		return this.treeSearch(pointLocation, Vector.zeros(pointLocation));
	}

		// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Find all agents that are potentially within the given distance of 
	 * a given focal agent.
	 * 
	 * @param anAgent Agent at the focus of this search.
	 * @param searchDist Distance around this agent to search.
	 * @return Collection of agents that may be within the search space: note
	 * that there may be some false positives (but no false negatives). The
	 * focal agent is not in this collection.
	 */
	public List<Agent> treeSearch(Agent anAgent, double searchDist)
	{
		// TODO not sure if this is the best response
		if ( ! IsLocated.isLocated(anAgent) )
			return new LinkedList<Agent>();
		/*
		 * Find all nearby agents.
		 */
		Body body = (Body) anAgent.get(AspectRef.agentBody);
		List<BoundingBox> boxes = body.getBoxes(searchDist, this.getShape());
		List<Agent> out = this.treeSearch(boxes);
		/* 
		 * Remove the focal agent from this list.
		 */
		IsSame isSame = new IsSame(anAgent);
		out.removeIf(isSame);
		// NOTE lambda expressions are known to be slow in java
		return out;
	}

	/**
	 * \brief Find all agents that are potentially within the
	 * given distance of a surface.
	 * @param aSurface Surface object belonging to this compartment.
	 * @param searchDist Find agents within this distance of the surface.
	 * @return Collection of agents that may be within the search distance of 
	 * the surface: there may be false positives, but no false negatives in 
	 * this collection.
	 */
	public Collection<Agent> treeSearch(Surface aSurface, double searchDist)
	{
		BoundingBox box = aSurface.getBox(searchDist, getShape());
		if ( box == null )
		{
			Log.out(CRITICAL, "Could not find bouding box for surface "+
					aSurface.toString());
		}
		return this.treeSearch(box);
	}
	
	/**
	 * \brief Find all agents that are potentially within the given distance of 
	 * a spatial boundary.
	 * 
	 * @param aBoundary Spatial boundary object belonging to this compartment.
	 * @param searchDist Find agents within this distance of the surface.
	 * @return Collection of agents that are potentially within the the search 
	 * distance of the boundary: there may be false positives, but no false 
	 * negatives in this collection.
	 */
	public Collection<Agent> treeSearch(
			SpatialBoundary aBoundary, double searchDist)
	{
		Surface surface = this._shape.getSurface(aBoundary);
		if ( surface == null )
		{
			Log.out(CRITICAL, "Could not find surface for boundary "+
					aBoundary.getDimName()+" "+aBoundary.getExtreme());
		}
		return this.treeSearch( surface , searchDist);
	}

		// FIXME move all aspect related methods out of general classes
	/**
	 * filter non colliding agents
	 * 
	 * FIXME not used -> remove?
	 * 
	 * @param aSurface
	 * @param agents
	 * @param searchDist
	 */
	public void filterAgentCollision(Surface aSurface, Collection<Agent> agents, double searchDist)
	{
		/* the collision object */
		Collision collision = new Collision(this._shape);
		for ( Agent a : agents)
		{
			/* by default assume no collision */
			boolean c = false;
			/* check each agent surface for collision */
			for ( Surface s : ((Body) a.get(AspectRef.agentBody)).getSurfaces())
			{
				/* on collision set boolean true and exit loop */
				if ( collision.areColliding(aSurface, s, searchDist))
				{
					c = true;
					break;
				}
			}
			/* if not in collision remove the agent */
			if ( !c )
				agents.remove(a);
		}	
	}
	
	/**
	 * 
	 * FIXME not used -> remove?
	 * 
	 * @param aSurface
	 * @param surfaces
	 * @param searchDist
	 */
	public void filterSurfaceCollision(Surface aSurface, Collection<Surface> surfaces, double searchDist)
	{
		/* the collision object */
		Collision collision = new Collision(this._shape);

		/* check each surface for collision, remove if not */
		for ( Surface s : surfaces)
			if ( ! collision.areColliding(aSurface, s, searchDist))
				surfaces.remove(s);
	}
	
		// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Find all boundary surfaces that the given agent may be close to.
	 * 
	 * @param anAgent Agent at the focus of this search.
	 * @param searchDist Distance around this agent to search.
	 * @return Collection of boundary surfaces that may be within the search
	 * space: note that there may be some false positives (but no false
	 * negatives). 
	 */
	public Collection<Surface> surfaceSearch(Agent anAgent, double searchDist)
	{
		IsNotColliding<Surface> filter;
		Collection<Surface> out = this._shape.getSurfaces();
		Collision collision = new Collision(this._shape);
		/* NOTE if the agent has many surfaces it may be faster the other way
		 * around  */
		Collection<Surface> agentSurfs = 
				((Body) anAgent.get(AspectRef.agentBody)).getSurfaces();
		filter = new IsNotColliding<Surface>(agentSurfs, collision, searchDist);
		out.removeIf(filter);
		return out;
	}

	/**
	 * \brief Find all boundary objects that the given agent may be close to.
	 * 
	 * @param anAgent Agent at the focus of this search.
	 * @param searchDist Distance around this agent to search.
	 * @return Collection of boundaries that may be within the search space:
	 * note that there may be some false positives (but no false negatives). 
	 */
	public Collection<SpatialBoundary> boundarySearch(
			Agent anAgent, double searchDist)
	{
		Collection<SpatialBoundary> out = new LinkedList<SpatialBoundary>();
		for ( Surface s : this.surfaceSearch(anAgent, searchDist) )
			out.add(this._shape.getBoundary(s));
		return out;
	}

	/* ***********************************************************************
	 * AGENT LOCATION & MASS
	 * **********************************************************************/

	// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Move the given agent along the given dimension, by the given
	 * distance.
	 * 
	 * @param anAgent Agent to move.
	 * @param dimN Name of the dimension: must be present to this compartment's
	 * shape.
	 * @param dist Distance to move: can be positive or negative.
	 */
	public void moveAlongDimension(Agent anAgent, DimName dimN, double dist)
	{
		if ( ! IsLocated.isLocated(anAgent) )
			return;
		Body body = (Body) anAgent.get(AspectRef.agentBody);
		double[] newLoc = body.getPoints().get(0).getPosition();
		this._shape.moveAlongDimension(newLoc, dimN, dist);
		body.relocate(newLoc);
		Log.out(DEBUG, "Moving agent (UID: "+anAgent.identity()+") "+dist+
				" along dimension "+dimN+" to "+Vector.toString(newLoc));
	}
	
	/*
	 * Note: process manager related methods moved to
	 */

	/* ***********************************************************************
	 * ADDING & REMOVING AGENTS
	 * **********************************************************************/

	/**
	 * \brief Add the given <b>agent</b> to the appropriate list.
	 * 
	 * @param agent {@code Agent} object to be accepted into this
	 * {@code AgentContainer}.
	 */
	public void addAgent(Agent agent)
	{
		if ( IsLocated.isLocated(agent) && this.getShape().getNumberOfDimensions() > 0 )
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
		anAgent.event(AspectRef.agentUpdateBody); /* hard coded should not be here */
		this._locatedAgentList.add(anAgent);
		this.treeInsert(anAgent);
	}

		// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Insert the given agent into this container's spatial registry.
	 * 
	 * <p><b>Important</b>: the agent bounding box should encapsulate the
	 * entire influence region of the agent (i.e. including pull distance).</p>
	 * 
	 * @param anAgent Agent to insert.
	 */
	private void treeInsert(Agent anAgent)
	{
		Body body = ((Body) anAgent.get(AspectRef.agentBody));
		Double dist = anAgent.getDouble(AspectRef.agentPulldistance);
		dist = Helper.setIfNone(dist, 0.0);
		List<BoundingBox> boxes = body.getBoxes(dist, this.getShape());
		for ( BoundingBox b: boxes )
			this._agentTree.insert(b, anAgent);
	}

	/**
	 * \brief Rebuild the spatial registry, by removing and then re-inserting 
	 * all located agents.
	 */
	public void refreshSpatialRegistry()
	{
		this._agentTree.clear();
		for ( Agent a : this.getAllLocatedAgents() )
			this.treeInsert(a);
	}

	/**
	 * \brief Choose a single agent by its place in the combined lists
	 * (located plus unlocated).
	 * 
	 * @param i Integer place in this container's lists. Should be less that
	 * {@link #getNumAllAgents()}
	 * @return This agent (still contained here).
	 * @see #chooseRandomAgent()
	 */
	public Agent chooseAgent(int i)
	{
		Tier level = Tier.NORMAL;
		if ( i > this.getNumAllAgents()-1 && Log.shouldWrite(level) )
		{
			Log.out(level, "AgentContainer chooseAgent out of bounds");
			return null;
		}
		else
			return (i > this._agentList.size()) ?
					/* Located agent. */
					this._locatedAgentList.get(i - this._agentList.size()) :
						/* non-located agent. */
						this._agentList.get(i);
	}

	/**
	 * @return A random {@code Agent}, without removing it from this container.
	 * @see #extractRandomAgent()
	 */
	public Agent chooseRandomAgent()
	{
		return this.chooseAgent( ExtraMath.getUniRandInt(
				this.getNumAllAgents() ) );
	}

	/**
	 * @return A randomly chosen {@code Agent}, who is removed from this
	 * container.
	 * @see #chooseRandomAgent()
	 */
	public Agent extractRandomAgent()
	{
		int nAgents = this.getNumAllAgents();
		/* Safety if there are no agents. */
		if ( nAgents == 0 )
		{
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
		if ( IsLocated.isLocated(anAgent) )
		{
			this._locatedAgentList.remove(anAgent);
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
		Dimension dim;
		for ( DimName dimN : this._shape.getDimensionNames() )
		{
			dim = this._shape.getDimension(dimN);
			if ( dim.isCyclic() )
			{
				continue;
			}
			if ( ! dim.isSignificant() )
			{
				continue;
			}
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				if ( ! dim.isBoundaryDefined(extreme) )
				{
					continue;
				}
				dim.getBoundary(extreme).agentsArrive();
			}
		}
		for ( Boundary bndry : this._shape.getNonSpatialBoundaries() )
		{
			bndry.agentsArrive();
		}
	}

	/**
	 * 
	 * \brief Loop through all boundaries on this shape, try to transfer agents
	 * over the boundaries
	 * 
	 * <p>If multiple boundaries want to transfer the same agent, choose one of
	 * these by random.</p>
	 */
	public void boundariesGrabAgents()
	{
		Map<Agent,List<Boundary>> toTransfer = 
				new HashMap<Agent,List<Boundary>>();
		/*
		 * Ask each boundary which agents it wants to grab. Since more than one
		 * boundary may want the same agent, compile a list for each agent.
		 */
		for ( Boundary b : this._shape.getAllBoundaries() )
		{
			Collection<Agent> boundaryDepartures = b.agentsToGrab();
			for ( Agent wishAgent : boundaryDepartures )
			{
				if ( ! toTransfer.containsKey( wishAgent ) )
					toTransfer.put(wishAgent, new LinkedList<Boundary>());
				toTransfer.get(wishAgent).add(b);
			}
		}
		/*
		 * For each of the leaving agents, see how many boundaries tried to
		 * remove it. If only one, then simply depart through this boundary.
		 * If more than one, then pick one at random.
		 */
		for ( Agent grabbedAgent : toTransfer.keySet() )
		{
			List<Boundary> destinations = toTransfer.get(grabbedAgent);
			if ( destinations.size() == 1 )
				/* add out-bound agent to departures list, agents are added to 
				 * the the arrivals list of the partner boundary when the agents
				 * are pushed */
				destinations.get(0).addOutboundAgent( grabbedAgent );
			else
			{
				int i = ExtraMath.getUniRandInt(destinations.size());
				destinations.get(i).addOutboundAgent(grabbedAgent);
			}
		}
	}
	
	/**
	 * \brief Loop over all boundaries, asking any agents waiting in their
	 * departure lounges to leave the compartment.
	 */
	public void agentsDepart()
	{
		Dimension dim;
		for ( DimName dimN : this._shape.getDimensionNames() )
		{
			dim = this._shape.getDimension(dimN);
			if ( dim.isCyclic() )
				continue;
			if ( ! dim.isSignificant() )
				continue;
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				if ( ! dim.isBoundaryDefined(extreme) )
					continue;
				dim.getBoundary(extreme).pushAllOutboundAgents();
			}
		}
		for ( Boundary bndry : this._shape.getNonSpatialBoundaries() )
			bndry.pushAllOutboundAgents();
	}

	/**
	 * @return {@code true} is this has any agents to report and destroy.
	 */
	public boolean hasAgentsToRegisterRemoved()
	{
		return ( ! this._agentsToRegisterRemoved.isEmpty() );
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
	
	/*
	 * NOTE: reaction diffusion clean-up methods moved to ProcessDiffusion
	 * post step.
	 */

	@Override
	public Module getModule() 
	{
		/* The agents node. */
		Module modelNode = new Module( XmlRef.agents, this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		/* Add the agent childConstrutor for adding of additional agents. */
		modelNode.addChildSpec( ClassRef.agent,
				Module.Requirements.ZERO_TO_MANY);
		/* If there are agents, add them as child nodes. */
		for ( Agent a : this.getAllAgents() )
			modelNode.add( a.getModule() );
		return modelNode;
	
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}

	public void sortLocatedAgents() 
	{
		for(Agent a : this.getAllUnlocatedAgents())
			if( IsLocated.isLocated(a) )
			{
				this._agentList.remove(a);
				this._locatedAgentList.add(a);
			}
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/

}
