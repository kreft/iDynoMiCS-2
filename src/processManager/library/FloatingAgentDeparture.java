package processManager.library;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import boundary.SpatialBoundary;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessDeparture;
import referenceLibrary.AspectRef;
import surface.Surface;
import surface.collision.Collision;

/**
 * This process manager removes groups of floating agents from a spatial
 * compartment. Groups of agents are identified by doing a neighbour search for
 * each agent, with a radius determined by _searchDistance. Then any groups or
 * individual agents that are not in contact with a solid boundary are 
 * considered to be floating and removed.
 * @author Tim
 *
 */
public class FloatingAgentDeparture extends ProcessDeparture {

	public static String SEARCH_DISTANCE = 
			AspectRef.collisionSearchDistance;
	
	private double _searchDistance;
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this._searchDistance = this.getDouble(SEARCH_DISTANCE);
	}
	
	
	@Override
	protected LinkedList<Agent> agentsDepart()
	{
		/*
		 * A map matching agent IDs to social network "bins".
		 */
		HashMap<Integer, LinkedList<Agent>> binMap = 
				new HashMap<Integer, LinkedList<Agent>>();
		
		/*
		 * List of all bins that have not been tagged as "attached". Each bin
		 * contains a collection of agents that form a neighbourhood. Bins in 
		 * this list may be subsequently identified as being attached and moved
		 * to attachedBins.
		 */
		LinkedList<LinkedList<Agent>> unattachedBins = 
				new LinkedList<LinkedList<Agent>>();
		
		/*
		 * List of all bins that have  been tagged as "attached". Each bin
		 * contains a collection of agents that form a neighbourhood.
		 */
		LinkedList<LinkedList<Agent>> attachedBins = 
				new LinkedList<LinkedList<Agent>>();
		
		/*
		 * Collision object used to measure distance between surfaces in this
		 * compartment
		 */
		Collision collision = new Collision(this._shape);
		
		
		/*
		 * All bins that contain either the focal agent, or a true neighbour
		 * of the focal agent
		 */
		LinkedList<LinkedList<Agent>> neighbouringBins = 
				new LinkedList<LinkedList<Agent>>();
		
		/*
		 * The bin that receives neighbouring agents
		 */
		LinkedList<Agent> receivingBin;
		
		
		/*
		 * Bins that are to be merged with the receiving bin.
		 */
		LinkedList<LinkedList<Agent>> binsToMerge =
				new LinkedList<LinkedList<Agent>>();
		
		
		/*
		 * Floating agents to be removed
		 */
		LinkedList<Agent> agentsToRemove = new LinkedList<Agent>();
		
		/*
		 * Whether or not the focal agent and its neighbourhood are considered
		 * to be attached to a surface of the compartment.
		 */
		Boolean attached;
		
		
		for ( Agent focalAgent : this._agents.getAllAgents() )
		{
			/*
			 * By default, agents are assumed to be floating.
			 */
			attached = false;
			
			
			/*
			 * Check whether the focal agent is in contact with a
			 * solid boundary. If so, it is considered attached.
			 */
			Collection<SpatialBoundary> collidingBoundaries = this._agents.
					boundarySearch(focalAgent, this._searchDistance);
			
			for (SpatialBoundary boundary: collidingBoundaries)
			{
				if (boundary.isSolid())
				{
					attached = true;
				}
			}
			
			
			List<Agent> neighbours = 
					this._agents.treeSearch(focalAgent, this._searchDistance);
			
			/*
			 * True neighbours - a subset of neighbours that are within the
			 * search distance of the focal agent.
			 */
			LinkedList<Agent> trueNeighbours = new LinkedList<Agent>();
			
			
			/*
			 * Check distances between the focal agent and all neighbours
			 * in order to populate trueNeighbours.
			 */
			
			Body agentBody = (Body) focalAgent.get(AspectRef.agentBody);
			
			List<Surface> agentSurfaces = agentBody.getSurfaces();
			
			for (Agent n : neighbours)
			{
				Body neighbourBody = (Body) n.get(AspectRef.agentBody);
				List<Surface> neighbourSurfaces = neighbourBody.getSurfaces();
				
				for (Surface agentSurf : agentSurfaces)
				{
					for (Surface neighbourSurf : neighbourSurfaces)
					{
						if (collision.distance(agentSurf, neighbourSurf)
								< this._searchDistance)
						{
							trueNeighbours.add(n);
						}
					}
				}
			}
			
			
			int agentID = focalAgent.identity();
			
			/*
			 * Check whether the focal agent has already been assigned to a bin.
			 * If so, this is added to neighbouringBins
			 */
			if (binMap.containsKey(agentID))
				neighbouringBins.add(binMap.get(agentID));
			
			/*
			 * Check whether neighbours already belong to any bins. If so, these
			 * are added to neighbouringBins.
			 */
			for (Agent n : trueNeighbours)
			{
				int neighbourID = n.identity();
				if (binMap.containsKey(neighbourID))
				{
					LinkedList<Agent> neighbouringBin = binMap.get(neighbourID);
					if (!neighbouringBins.contains(neighbouringBin))
							neighbouringBins.add(binMap.get(neighbourID));
				}
			}
			
			
			if (neighbouringBins.isEmpty())
			{
				receivingBin = new LinkedList<Agent>();
				
				/*
				 * By default, assume the receiving bin is not attached.
				 */
				unattachedBins.add(receivingBin);
			}
			
			else
			{
				receivingBin = neighbouringBins.getFirst();
				
				/*
				 * If any bin in the neighbouringBins list is attached, then
				 * the focal agent must be part of an attached group.
				 */
				for (LinkedList<Agent> bin : neighbouringBins)
				{
					if (attachedBins.contains(bin))
					{
						attached = true;
					}
				}
				
				/*
				 * Populates binsToMerge with all but the first bin in
				 * neighbouringBins.
				 */
				for (int i = 1; i < neighbouringBins.size() - 1; i++)
				{
					binsToMerge.add(neighbouringBins.get(i));
				}
			}
			
			neighbouringBins.clear();
			
			
			/*
			 * Add the focal agent to the receiving bin if it is not already
			 * recorded in the binMap. If it already recorded then it belongs
			 * to a bin which will be merged later or the receiving bin.
			 */
			if (!binMap.containsKey(agentID))
			{
				receivingBin.add(focalAgent);
				/*
				 * Record the focal agent as belonging to the receiving bin
				 */
				binMap.put(agentID, receivingBin);
			}
			
			/*
			 * Do the same thing for all neighbours
			 */
			for (Agent n : trueNeighbours)
			{
				int neighbourID = n.identity();
				
				if (!binMap.containsKey(neighbourID))
				{
					receivingBin.add(n);
					binMap.put(neighbourID, receivingBin);
				}
			}
			
			/*
			 * Add the contents of merging bins to the receiving bin
			 */
			for (LinkedList<Agent> bin : binsToMerge)
			{
				for (Agent a : bin)
				{
					int id = a.identity();
					receivingBin.add(a);
					binMap.put(id, receivingBin);
				}
			}
			
			/*
			 * Having merged the merging bins, remove them
			 */
			for (LinkedList<Agent> bin : binsToMerge)
			{
				if (unattachedBins.contains(bin))
					unattachedBins.remove(bin);
				else
					attachedBins.remove(bin);
			}
			
			binsToMerge.clear();
			
			/*
			 * If the current agent or any of the groups it has now merged with
			 * are considered attached, the receiving bin is recorded as being
			 * attached.
			 */
			if (attached)
			{
				if (!attachedBins.contains(receivingBin))
				{
					attachedBins.add(receivingBin);
					unattachedBins.remove(receivingBin);
				}
			}
		}
		
		/*
		 * The contents of bins that are not considered attached are added to
		 * the lists of agents to remove.
		 */
		for (LinkedList<Agent> bin : unattachedBins)
		{
			agentsToRemove.addAll(bin);
		}
		
		return agentsToRemove;
	}

}
