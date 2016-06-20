package boundary.spatialLibrary;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import aspect.AspectRef;
import boundary.SpatialBoundary;
import boundary.library.ChemostatToBoundaryLayer;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import shape.Dimension.DimName;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class BiofilmBoundaryLayer extends SpatialBoundary
{
	/**
	 * Boundary layer thickness.
	 */
	// TODO set this from protocol file
	private double _layerTh = 10.0;
	/**
	 * For the random walk after insertion, we assume that the agent has the
	 * stochastic move event.
	 */
	// NOTE This is not a permanent solution.
	public static String STOCHASTIC_MOVE = AspectRef.agentStochasticMove;
	/**
	 * For the random walk after insertion, we assume that the agent has the
	 * pull distance aspect.
	 */
	// NOTE This is not a permanent solution.
	public static String CURRENT_PULL_DISTANCE = AspectRef.collisionCurrentPullDistance;
	/**
	 * For the random walk after insertion, we use an arbitrary time step size.
	 */
	// NOTE This is not a permanent solution.
	public static double MOVE_TSTEP = 1.0;
	
	/**
	 * \brief Log file verbosity level used for debugging agent arrival.
	 * 
	 * <ul><li>Set to {@code BULK} for normal simulations</li>
	 * <li>Set to {@code DEBUG} when trying to debug an issue</li></ul>
	 */
	private static final Tier AGENT_ARRIVE_LEVEL = Tier.DEBUG;
	
	/* ***********************************************************************
	 * CONSTRUCTOR
	 * **********************************************************************/
	
	/**
	 * \brief Construct a biofilm boundary layer by giving it the information
	 * it needs about its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public BiofilmBoundaryLayer(DimName dim, int extreme)
	{
		super(dim, extreme);
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
	{
		return ChemostatToBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void updateConcentrations(EnvironmentContainer environment)
	{
		// TODO
	}

	@Override
	public double getFlux(SpatialGrid grid)
	{
		// TODO
		return 0.0;
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	@Override
	public void agentsArrive(AgentContainer agentCont)
	{
		/*
		 * Give all (located) agents a random position along this boundary
		 * surface. Unlocated agents can be simply added to the Compartment.
		 */
		this.placeAgentsRandom(agentCont);
		/*
		 * Calculate the step size and direction that agents will use to
		 * move.
		 */
		// NOTE Rob [19/5/2016]: the value of 0.1 is arbitrary.
		double dist = 0.1 * this._layerTh;
		if ( this._extreme == 1 )
			dist = -dist;
		/*
		 * Move each agent away from the boundary surface until it reaches
		 * the top of the boundary layer.
		 */
		Collection<Agent> nbhAgents;
		Collection<SpatialBoundary> bndries;
		for ( Agent anAgent : this._arrivalsLounge )
		{
			Log.out(AGENT_ARRIVE_LEVEL, "Moving agent (UID: "+
					anAgent.identity()+") to top of boundary layer");
			/*
			 * Move the agent down from the boundary surface to the top of the
			 * boundary layer.
			 */
			insertionLoop: while ( true )
			{
				nbhAgents = agentCont.treeSearch(anAgent, this._layerTh);
				if ( ! nbhAgents.isEmpty() )
					break insertionLoop;
				bndries = agentCont.boundarySearch(anAgent, this._layerTh);
				if ( ! bndries.isEmpty() )
				{
					// FIXME stopping is a temporary fix: we need to apply
					// the boundary here
					break insertionLoop;
				}
				agentCont.moveAlongDimension(anAgent, this._dim, dist);
			}
			/*
			 * Now that the agent is at the top of the boundary layer, perform
			 * a random walk until it hits a boundary or another agent.
			 */
			double pull = anAgent.getDouble(CURRENT_PULL_DISTANCE);
			Log.out(AGENT_ARRIVE_LEVEL, "Now attemting random walk: using "+
					pull+" for pull distance");
			randomLoop: while ( true )
			{
				/*
				 * Find all boundaries this agent has collided with.
				 */
				bndries = agentCont.boundarySearch(anAgent, pull);
				/*
				 * If the agent has wandered up and collided with this
				 * boundary, re-insert it at the back of the arrivals lounge
				 */
				if ( bndries.contains(this) )
				{
					this._arrivalsLounge.remove(anAgent);
					this._arrivalsLounge.add(anAgent);
					Log.out(AGENT_ARRIVE_LEVEL,
						"Agent has returned to boundary: re-inserting later");
					break randomLoop;
				}
				/*
				 * If the agent has collided with another boundary, TODO
				 */
				if ( ! bndries.isEmpty() )
				{
					// FIXME Assume the boundary is solid for now
					agentCont.addAgent(anAgent);
					Log.out(AGENT_ARRIVE_LEVEL,
							"Agent has hit another boundary");
					break randomLoop;
				}
				/*
				 * The agent has not collided with any boundaries, so see if it
				 * has collided with any other agents.
				 */
				nbhAgents = agentCont.treeSearch(anAgent, pull);
				/*
				 * If the agent has collided with others, add it to the agent
				 * container and continue to the next agent.
				 */
				if ( ! nbhAgents.isEmpty() )
				{
					// TODO use the pulling method in Collision?
					agentCont.addAgent(anAgent);
					Log.out(AGENT_ARRIVE_LEVEL,
							"Agent has hit another agent");
					break randomLoop;
				}
				/*
				 * Ask the agent to move in a random walk.
				 */
				anAgent.event(STOCHASTIC_MOVE, MOVE_TSTEP);
			}
		}
		this.clearArrivalsLoungue();
	}

	@Override
	public List<Agent> agentsToGrab(AgentContainer agentCont)
	{
		List<Agent> out = new LinkedList<Agent>();
		/*
		 * Find all agents who are unattached to others or to a boundary,
		 * and who are on this side of the biofilm (in, e.g., the case of a
		 * floating granule).
		 */
		// TODO
		return out;
	}
}
