package idynomics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

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
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.SubvoxelPoint;
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

	/**
	 * Helper method for (re-)making this container's spatial registry.
	 */
	protected void makeAgentTree()
	{
		if ( this.getNumDims() == 0 )
			this._agentTree = new DummyTree<Agent>();
		else
		{
			/*
			 * Bas: I have chosen maxEntries and minEntries by testing what
			 * values resulted in fast tree creation and agent searches.
			 */
			// TODO R-tree parameters could follow from the protocol file.
			this._agentTree = new RTree<Agent>(8, 2, this._shape);
		}
	}

	/**
	 * \brief Construct agents from a list of XML nodes.
	 * 
	 * @param agentNodes List of XML nodes from a protocol file.
	 * @param comp Compartment that this container belongs to.
	 */
	public void readAgents(NodeList agentNodes, Compartment comp)
	{
		for ( int i = 0; i < agentNodes.getLength(); i++ ) 
			this.addAgent(new Agent(agentNodes.item(i), comp));
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
		for ( Agent a : this._agentTree.all() )
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

	/**
	 * \brief Find all agents within the given distance of a given focal agent.
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
		out.removeIf((a) -> {return a == anAgent;});
		return out;
	}

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
			out.add(this._shape.getSurfaceBounds().get(s));
		return out;
	}

	/* ***********************************************************************
	 * AGENT LOCATION
	 * **********************************************************************/

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
		Log.out(DEBUG, "Moving agent (UID: "+anAgent.identity()+
				") along dimension "+dimN+" to "+Vector.toString(newLoc));
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
		this._locatedAgentList.add(anAgent);
		this.treeInsert(anAgent);
	}

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
		double dist = 0.0;
		if ( anAgent.isAspect(AspectRef.agentPulldistance) )
			dist = anAgent.getDouble(AspectRef.agentPulldistance);
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

	/* ***********************************************************************
	 * AGENT MASS DISTRIBUTION
	 * **********************************************************************/
	
	/**
	 * \brief Loop through all located {@code Agent}s with reactions,
	 * estimating how much of their body overlaps with nearby grid voxels.
	 * 
	 * @param agents The agents of a {@code Compartment}.
	 */
	@SuppressWarnings("unchecked")
	public void setupAgentDistributionMaps()
	{
		Log.out(DEBUG, "Setting up agent distribution maps");
		Tier level = BULK;
		/*
		 * Reset the agent biomass distribution maps.
		 */
		CoordinateMap distributionMap;
		for ( Agent a : this.getAllLocatedAgents() )
		{
			distributionMap = new CoordinateMap();
			a.set(VD_TAG, distributionMap);
		}
		/*
		 * Now fill these agent biomass distribution maps.
		 */
		Shape shape = this.getShape();
		int nDim = this.getNumDims();
		double[] location;
		double[] dimension = new double[3];
		double[] sides;
		List<SubvoxelPoint> svPoints;
		List<Agent> nhbs;
		List<Surface> surfaces;
		double[] pLoc;
		Collision collision = new Collision(null, shape);
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext())
		{
			/* Find all agents that overlap with this voxel. */
			// TODO a method for getting a voxel's bounding box directly?
			location = Vector.subset(shape.getVoxelOrigin(coord), nDim);
			shape.getVoxelSideLengthsTo(dimension, coord);
			sides = Vector.subset(dimension, nDim);
			/* NOTE the agent tree is always the amount of actual dimension */
			nhbs = this.treeSearch(location, sides);
			/* Filter the agents for those with reactions, radius & surface. */
			nhbs.removeIf(NO_REAC_FILTER);
			nhbs.removeIf(NO_BODY_FILTER);
			/* If there are none, move onto the next voxel. */
			if ( nhbs.isEmpty() )
				continue;
			Log.out(level, "  "+nhbs.size()+" agents overlap with coord "+
					Vector.toString(coord));
			/* 
			 * Find the sub-voxel resolution from the smallest agent, and
			 * get the list of sub-voxel points.
			 */
			double minRad = Vector.min(sides);
			double radius;
			for ( Agent a : nhbs )
			{
				radius = a.getDouble(AspectRef.bodyRadius);
				Log.out(level, "   agent "+a.identity()+" has radius "+radius);
				minRad = Math.min(radius, minRad);
			}
			minRad *= SUBGRID_FACTOR;
			Log.out(level, "  using a min radius of "+minRad);
			svPoints = shape.getCurrentSubvoxelPoints(minRad);
			Log.out(level, "  gives "+svPoints.size()+" sub-voxel points");
			/* Get the sub-voxel points and query the agents. */
			for ( Agent a : nhbs )
			{
				/* Should have been removed, but doesn't hurt to check. */
				if ( ! a.isAspect(AspectRef.agentReactions) )
					continue;
				if ( ! a.isAspect(AspectRef.surfaceList) )
					continue;
				surfaces = (List<Surface>) a.get(AspectRef.surfaceList);
				Log.out(level, "  "+"   agent "+a.identity()+" has "+
						surfaces.size()+" surfaces");
				distributionMap = (CoordinateMap) a.getValue(VD_TAG);
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
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/

}
