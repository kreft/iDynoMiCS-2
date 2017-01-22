package idynomics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import agent.Agent;
import agent.Body;
import boundary.Boundary;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import gereralPredicates.IsSame;

import static dataIO.Log.Tier.*;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.CartesianShape;
import shape.Dimension;
import shape.Shape;
import shape.Dimension.DimName;
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.SubvoxelPoint;
import solver.PDEsolver;
import spatialRegistry.*;
import spatialRegistry.splitTree.SplitTree;
import surface.BoundingBox;
import surface.Collision;
import surface.predicate.IsNotColliding;
import surface.Surface;
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
	 * TODO
	 */
	protected SpatialGrid _detachability;
	/**
	 * TODO
	 */
	public final static String DETACHABILITY = "detachability";
	
	protected PDEsolver _detachabilitySolver;
	
	/**
	 * TODO
	 */
	private Settable _parentNode;
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	protected final static Predicate<Agent> NO_REAC_FILTER = 
			(a -> ! a.isAspect(AspectRef.agentReactions));
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have relevant components of a body.
	 */
	protected final static Predicate<Agent> NO_BODY_FILTER = 
			(a -> (! a.isAspect(AspectRef.surfaceList)) ||
					( ! a.isAspect(AspectRef.bodyRadius)));
	/**
	 * When choosing an appropriate sub-voxel resolution for building agents'
	 * {@code coordinateMap}s, the smallest agent radius is multiplied by this
	 * factor to ensure it is fine enough.
	 */
	// NOTE the value of a quarter is chosen arbitrarily
	private static double SUBGRID_FACTOR = 0.25;
	/**
	 * Aspect name for the {@code coordinateMap} used for establishing which
	 * voxels a located {@code Agent} covers.
	 */
	private static final String VD_TAG = AspectRef.agentVolumeDistributionMap;
	
	/**
	 * the type of spatial registry ( setting default value but can be 
	 * overwritten).
	 */
	private TreeType _spatialTree = TreeType.RTREE;
	
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

	public void setSpatialTree(TreeType type) 
	{
		this._spatialTree = type;
	}
	
	public TreeType getSpatialTree() 
	{
		return this._spatialTree;
	}

	/**
	 * Helper method for (re-)making this container's spatial registry.
	 */
	protected void makeAgentTree()
	{
		if ( this.getNumDims() == 0 )
			this._agentTree = new DummyTree<Agent>();
		else
		{
			switch (_spatialTree)
			{
			case RTREE:
				this._agentTree = new RTree<Agent>(8, 2, this._shape);
				break;
			case SPLITTREE:
				/* currently domain minimum is always set to zero but this will
				 * change, min represents domain minima */
				double[] min = Vector.zerosDbl(
						this.getShape().getNumberOfDimensions() );
				/* 
				 * FIXME when more than max_entries agents overlap in on position
				 *  the split tree will cause a stack overflow exception
				 */
				this._agentTree = new SplitTree<Agent>(this.getNumDims(), 3, 24, 
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
		return this._agentTree.cyclicsearch(boundingBox);
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
		return this._agentTree.cyclicsearch(boundingBoxes);
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
		return this._agentTree.cyclicsearch(location, dimensions);
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
		BoundingBox box = aSurface.getBox(searchDist);
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
		// NOTE lambda expressions are known to be slower than alternatives
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
		if ( ! isLocated(anAgent) )
			return;
		Body body = (Body) anAgent.get(AspectRef.agentBody);
		double[] newLoc = body.getPoints().get(0).getPosition();
		this._shape.moveAlongDimension(newLoc, dimN, dist);
		body.relocate(newLoc);
		Log.out(DEBUG, "Moving agent (UID: "+anAgent.identity()+") "+dist+
				" along dimension "+dimN+" to "+Vector.toString(newLoc));
	}

	// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Compose a dictionary of biomass names and values for the given
	 * agent.
	 * 
	 * <p>this method is the opposite of 
	 * {@link #updateAgentMass(Agent, HashMap<String,Double>)}.</p>
	 * 
	 * @param agent An agent with biomass.
	 * @return Dictionary of biomass kind names to their values.
	 */
	// TODO move this, and updateAgentMass(), to somewhere more general?
	public static Map<String,Double> getAgentMassMap(Agent agent)
	{
		Map<String,Double> out = new HashMap<String,Double>();
		Object mass = agent.get(AspectRef.agentMass);
		if ( mass == null )
		{
			// TODO safety?
		}
		else if ( mass instanceof Double )
		{
			out.put(AspectRef.agentMass, ((double) mass));
		}
		else if ( mass instanceof Map )
		{
			/* If the mass object is already a map, then just copy it. */
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) mass;
			out.putAll(massMap);
		}
		else
		{
			// TODO safety?
		}
		return out;
	}

	// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Use a dictionary of biomass names and values to update the given
	 * agent.
	 * 
	 * <p>This method is the opposite of {@link #getAgentMassMap(Agent)}. Note
	 * that extra biomass types may have been added to the map, which should
	 * be other aspects (e.g. EPS).</p>
	 * 
	 * @param agent An agent with biomass.
	 * @param biomass Dictionary of biomass kind names to their values.
	 */
	public static void updateAgentMass(Agent agent, Map<String,Double> biomass)
	{
		/*
		 * First try to copy the new values over to the agent mass aspect.
		 * Remember to remove the key-value pairs from biomass, so that we can
		 * see what is left (if anything).
		 */
		Object mass = agent.get(AspectRef.agentMass);
		if ( mass == null )
		{
			// TODO safety?
		}
		else if ( mass instanceof Double )
		{
			/**
			 * NOTE map.remove returns the current associated value and removes
			 * it from the map
			 */
			agent.set(AspectRef.agentMass, biomass.remove(AspectRef.agentMass));
		}
		else if ( mass instanceof Map )
		{
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) mass;
			for ( String key : massMap.keySet() )
			{
				massMap.put(key, biomass.remove(key));
			}
			
			agent.set(AspectRef.agentMass, biomass);
		}
		else
		{
			// TODO safety?
		}
		/*
		 * Now check if any other aspects were added to biomass (e.g. EPS).
		 */
		for ( String key : biomass.keySet() )
		{
			if ( agent.isAspect(key) )
			{
				agent.set(key, biomass.get(key));
				biomass.remove(key);
			}
			else
			{
				// TODO safety
			}
		}
	}

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
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "No agents in this container, so cannot "+
						"choose one: returning null");
			}
			return null;
		}/* Now find an agent. */
		int i = ExtraMath.getUniRandInt(nAgents);
		Agent out = this.chooseAgent(i);
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "Out of "+nAgents+" agents, agent with UID "+
					out.identity()+" was chosen randomly");
		}
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
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "No agents in this container, so cannot "+
					"extract one: returning null");
			}
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
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "Out of "+nAgents+" agents, agent with UID "+
					out.identity()+" was extracted randomly");
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
		if ( isLocated(anAgent) )
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
		Tier level = BULK;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Agents arriving into compartment...");
		Dimension dim;
		for ( DimName dimN : this._shape.getDimensionNames() )
		{
			dim = this._shape.getDimension(dimN);
			if ( dim.isCyclic() )
			{
				if ( Log.shouldWrite(level) )
					Log.out(level, "   "+dimN+" is cyclic, skipping");
				continue;
			}
			if ( ! dim.isSignificant() )
			{
				if ( Log.shouldWrite(level) )
					Log.out(level, "   "+dimN+" is insignificant, skipping");
				continue;
			}
			for ( int extreme = 0; extreme < 2; extreme++ )
			{
				if ( Log.shouldWrite(level) )
				{
					Log.out(level,
							"Looking at "+dimN+" "+((extreme==0)?"min":"max"));
				}
				if ( ! dim.isBoundaryDefined(extreme) )
				{
					if ( Log.shouldWrite(level) )
						Log.out(level, "   boundary not defined");
					continue;
				}
				dim.getBoundary(extreme).agentsArrive();
				if ( Log.shouldWrite(level) )
					Log.out(level, "   boundary defined, agents ariving");
			}
		}
		for ( Boundary bndry : this._shape.getOtherBoundaries() )
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level,"   other boundary "+bndry.getName()+
						", calling agent method");
			}
			bndry.agentsArrive();
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " All agents have now arrived");
	}

	/**
	 * \brief Loop through all boundaries on this shape, trying to grab the
	 * agents each wants.
	 * 
	 * <p>If multiple boundaries want the same agent, choose one of these by
	 * random.</p>
	 */
	public void boundariesGrabAgents()
	{
		Map<Agent,List<Boundary>> grabs = 
				new HashMap<Agent,List<Boundary>>();
		/*
		 * Ask each boundary which agents it wants to grab. Since more than one
		 * boundary may want the same agent, compile a list for each agent.
		 */
		for ( Boundary b : this._shape.getAllBoundaries() )
		{
			Collection<Agent> wishes = b.agentsToGrab();
			for ( Agent wishAgent : wishes )
			{
				if ( ! grabs.containsKey(wishAgent) )
					grabs.put(wishAgent, new LinkedList<Boundary>());
				grabs.get(wishAgent).add(b);
			}
		}
		/*
		 * For each agent to be grabbed, see how many boundaries want it.
		 * If only one, then simply depart through this boundary.
		 * If more than one, then pick one at random.
		 */
		for ( Agent grabbedAgent : grabs.keySet() )
		{
			List<Boundary> destinations = grabs.get(grabbedAgent);
			if ( destinations.size() == 1 )
				destinations.get(0).addOutboundAgent(grabbedAgent);
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
		Tier level = BULK;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Pushing all outbound agents...");
		Dimension dim;
		for ( DimName dimN : this._shape.getDimensionNames() )
		{
			dim = this._shape.getDimension(dimN);
			if ( dim.isCyclic() )
			{
				if ( Log.shouldWrite(level) )
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
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, 
						"Looking at "+dimN+" "+((extreme==0)?"min":"max"));
				}
				if ( ! dim.isBoundaryDefined(extreme) )
				{
					if ( Log.shouldWrite(level) )
						Log.out(level, "   boundary not defined");
					continue;
				}
				if ( Log.shouldWrite(level) )
					Log.out(level, "   boundary defined, pushing agents");
				dim.getBoundary(extreme).pushAllOutboundAgents();
			}
		}
		for ( Boundary bndry : this._shape.getOtherBoundaries() )
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(level,"   other boundary "+bndry.getName()+
						", pushing agents");
			}
			bndry.pushAllOutboundAgents();
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " All agents have now departed");
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

	/* ***********************************************************************
	 * AGENT MASS DISTRIBUTION
	 * **********************************************************************/

	// FIXME move all aspect related methods out of general classes
	/**
	 * \brief Loop through all located {@code Agent}s with reactions,
	 * estimating how much of their body overlaps with nearby grid voxels.
	 * 
	 * @see #removeAgentDistibutionMaps()
	 */
	@SuppressWarnings("unchecked")
	public void setupAgentDistributionMaps(Shape shape)
	{
		Tier level = BULK;
		if (Log.shouldWrite(level))
			Log.out(level, "Setting up agent distribution maps");
		
		/*
		 * Reset the agent biomass distribution maps.
		 */
		Map<Shape, CoordinateMap> mapOfMaps;
		for ( Agent a : this.getAllLocatedAgents() )
		{
			if ( a.isAspect(VD_TAG) )
				mapOfMaps = (Map<Shape, CoordinateMap>)a.get(VD_TAG);
			else
				mapOfMaps = new HashMap<Shape, CoordinateMap>();
			mapOfMaps.put(shape, new CoordinateMap());
			a.set(VD_TAG, mapOfMaps);
		}
		/*
		 * Now fill these agent biomass distribution maps.
		 */
		int nDim = this.getNumDims();
		double[] location;
		double[] dimension = new double[3];
		double[] sides;
		Collection<SubvoxelPoint> svPoints;
		List<Agent> nhbs;
		List<Surface> surfaces;
		double[] pLoc;
		Collision collision = new Collision(null, shape);
		CoordinateMap distributionMap;

		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext())
		{
			double minRad;
			
			if( shape instanceof CartesianShape)
			{
				/* Find all agents that overlap with this voxel. */
				// TODO a method for getting a voxel's bounding box directly?
				location = Vector.subset(shape.getVoxelOrigin(coord), nDim);
				shape.getVoxelSideLengthsTo(dimension, coord); //FIXME returns arc lengths with polar coords
				// FIXME create a bounding box that always captures at least the complete voxel
				sides = Vector.subset(dimension, nDim);
				/* NOTE the agent tree is always the amount of actual dimension */
				nhbs = this.treeSearch(location, sides);
				
				/* used later to find subgridpoint scale */
				minRad = Vector.min(sides);
			}
			else
			{
				/* TODO since the previous does not work at all for polar */
				nhbs = this.getAllLocatedAgents();
				
				/* FIXME total mess, trying to get towards something that at
				 * least makes some sence
				 */
				shape.getVoxelSideLengthsTo(dimension, coord); //FIXME returns arc lengths with polar coords
				// FIXME create a bounding box that always captures at least the complete voxel
				sides = Vector.subset(dimension, nDim);
				// FIXME because it does not make any sence to use the ark, try the biggest (probably the R dimension) and half that to be safe.
				minRad = Vector.max(sides) / 2.0; 
			}
			/* Filter the agents for those with reactions, radius & surface. */
			nhbs.removeIf(NO_REAC_FILTER);
			nhbs.removeIf(NO_BODY_FILTER);
			/* If there are none, move onto the next voxel. */
			if ( nhbs.isEmpty() )
				continue;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  "+nhbs.size()+" agents overlap with coord "+
					Vector.toString(coord));
			}
			
			/* 
			 * Find the sub-voxel resolution from the smallest agent, and
			 * get the list of sub-voxel points.
			 */
			
			double radius;
			for ( Agent a : nhbs )
			{
				radius = a.getDouble(AspectRef.bodyRadius);
				Log.out(level, "   agent "+a.identity()+" has radius "+radius);
				minRad = Math.min(radius, minRad);
			}
			minRad *= SUBGRID_FACTOR;
			svPoints = shape.getCurrentSubvoxelPoints(minRad);
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  using a min radius of "+minRad);
				Log.out(level, "  gives "+svPoints.size()+" sub-voxel points");
			}
			/* Get the sub-voxel points and query the agents. */
			for ( Agent a : nhbs )
			{
				/* Should have been removed, but doesn't hurt to check. */
				if ( ! a.isAspect(AspectRef.agentReactions) )
					continue;
				if ( ! a.isAspect(AspectRef.surfaceList) )
					continue;
				surfaces = (List<Surface>) a.get(AspectRef.surfaceList);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "  "+"   agent "+a.identity()+" has "+
						surfaces.size()+" surfaces");
				}
				mapOfMaps = (Map<Shape, CoordinateMap>) a.getValue(VD_TAG);
				distributionMap = mapOfMaps.get(shape);
				sgLoop: for ( SubvoxelPoint p : svPoints )
				{
					/* Only give location in significant dimensions. */
					pLoc = p.getRealLocation(nDim);
					for ( Surface s : surfaces )
						if ( collision.distance(s, pLoc) < 0.0 )
						{
							distributionMap.increase(coord, p.volume);
							/*
							 * We only want to count this point once, even
							 * if other surfaces of the same agent hit it.
							 */
							continue sgLoop;
						}
				}
			}
		}
		Log.out(DEBUG, "Finished setting up agent distribution maps");
	}
	
	/**
	 * \brief Loop through all located {@code Agents}, removing their mass
	 * distribution maps.
	 * 
	 * <p>This prevents unneeded clutter in XML output.</p>
	 * 
	 * @see #setupAgentDistributionMaps()
	 */
	public void removeAgentDistibutionMaps()
	{
		for ( Agent a : this.getAllLocatedAgents() )
			a.reg().remove(VD_TAG);
	}

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
			if( isLocated(a))
			{
				this._agentList.remove(a);
				this._locatedAgentList.add(a);
			}
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/

}
