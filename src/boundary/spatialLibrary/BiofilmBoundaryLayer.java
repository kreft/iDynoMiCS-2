package boundary.spatialLibrary;

import static grid.ArrayType.WELLMIXED;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import boundary.SpatialBoundary;
import boundary.WellMixedBoundary;
import boundary.library.ChemostatToBoundaryLayer;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Ball;
import surface.BoundingBox;
import surface.Collision;
import surface.Surface;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class BiofilmBoundaryLayer extends WellMixedBoundary
{
	/**
	 * Spherical surface object with radius equal to {@link #_layerThickness}.
	 * Used here for updating the well-mixed array.
	 */
	protected Ball _gridSphere;
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
	
	public BiofilmBoundaryLayer()
	{}
	
	@Override
	public void setContainers(
			EnvironmentContainer environment, AgentContainer agents)
	{
		super.setContainers(environment, agents);
		this.tryToCreateGridSphere();
	}
	
	@Override
	public boolean isReadyForLaunch()
	{
		if ( ! super.isReadyForLaunch() )
			return false;
		return this._gridSphere != null;
	}

	private void tryToCreateGridSphere()
	{
		if ( this._agents == null || this._layerThickness <= 0.0 )
			return;
		
		Shape shape = this._agents.getShape();
		Collision collision = new Collision(null, shape);
		double[] zeros = Vector.zerosDbl(shape.getNumberOfDimensions());
		this._gridSphere = new Ball(zeros, 0.5 * this._layerThickness);
		this._gridSphere.init(collision);
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	@Override
	protected boolean needsLayerThickness()
	{
		return true;
	}

	@Override
	public void setLayerThickness(double thickness)
	{
		/*
		 * If the boundary layer thickness changes, we also need to change the 
		 * radius of the ball used in updating the well-mixed array.
		 * NOTE: One sets a Ball's radius, not diameter
		 */
		super.setLayerThickness(thickness);
		this.tryToCreateGridSphere();
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return ChemostatToBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	protected double calcDiffusiveFlow(SpatialGrid grid)
	{
		double concn = this._concns.get(grid.getName());
		return this.calcDiffusiveFlowFixed(grid, concn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateWellMixedArray()
	{
		Shape aShape = this._environment.getShape();
		SpatialGrid grid = this._environment.getCommonGrid();
		int numDim = aShape.getNumberOfDimensions();
		/*
		 * Iterate over all voxels, checking if there are agents nearby.
		 */
		int[] coords = aShape.resetIterator();
		double[] voxelCenter = aShape.getVoxelCentre(coords);
		double[] voxelCenterTrimmed = Vector.zerosDbl(numDim);
		List<Agent> neighbors;
		BoundingBox box;
		while ( aShape.isIteratorValid() )
		{
			aShape.voxelCentreTo(voxelCenter, coords);
			Vector.copyTo(voxelCenterTrimmed, voxelCenter);
			this._gridSphere.setCenter(voxelCenterTrimmed);
			/*
			 * Find all nearby agents. Set the grid to zero if an agent is
			 * within the grid's sphere
			 */
			box = this._gridSphere.boundingBox(this._agents.getShape());
			neighbors = this._agents.treeSearch(box);
			for ( Agent a : neighbors )
				for (Surface s : (List<Surface>) a.get(AspectRef.surfaceList))
					if ( this._gridSphere.distanceTo(s) < 0.0 )
						{
							grid.setValueAt(WELLMIXED, coords, 0.0);
							break;
						}
			coords = aShape.iteratorNext();
		}
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	@Override
	public void agentsArrive()
	{
		if ( this._arrivalsLounge.isEmpty() )
			return;
		/*
		 * Give all (located) agents a random position along this boundary
		 * surface. Unlocated agents can be simply added to the Compartment.
		 */
		this.placeAgentsRandom();
		/*
		 * Calculate the step size and direction that agents will use to
		 * move.
		 */
		// NOTE Rob [19/5/2016]: the value of 0.1 is arbitrary.
		double dist = 0.1 * this._layerThickness;
		if ( dist == 0.0 )
		{
			Log.out(Tier.CRITICAL, "Error! Layer thickness is zero");
			return;
		}
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
			if ( Log.shouldWrite(AGENT_ARRIVE_LEVEL) )
			{
				Log.out(AGENT_ARRIVE_LEVEL, "Moving agent (UID: "+
						anAgent.identity()+") to top of boundary layer");
			}
			/*
			 * Move the agent down from the boundary surface to the top of the
			 * boundary layer.
			 */
			insertionLoop: while ( true )
			{
				nbhAgents = this._agents.treeSearch(anAgent, this._layerThickness);
				if ( ! nbhAgents.isEmpty() )
					break insertionLoop;
				bndries = this._agents.boundarySearch(anAgent, this._layerThickness);
				bndries.remove(this);
				if ( ! bndries.isEmpty() )
				{
					// FIXME stopping is a temporary fix: we need to apply
					// the boundary here
					break insertionLoop;
				}
				this._agents.moveAlongDimension(anAgent, this._dim, dist);
			}
			/*
			 * Now that the agent is at the top of the boundary layer, perform
			 * a random walk until it hits a boundary or another agent.
			 */
			double pull = anAgent.getDouble(CURRENT_PULL_DISTANCE);
			if ( Log.shouldWrite(AGENT_ARRIVE_LEVEL) )
			{
				Log.out(AGENT_ARRIVE_LEVEL, "Now attemting random walk: using "+
						pull+" for pull distance");
			}
			randomLoop: while ( true )
			{
				/*
				 * Find all boundaries this agent has collided with.
				 */
				bndries = this._agents.boundarySearch(anAgent, pull);
				/*
				 * If the agent has wandered up and collided with this
				 * boundary, re-insert it at the back of the arrivals lounge
				 */
				if ( bndries.contains(this) )
				{
					this._arrivalsLounge.remove(anAgent);
					this._arrivalsLounge.add(anAgent);
					if ( Log.shouldWrite(AGENT_ARRIVE_LEVEL) )
					{
						Log.out(AGENT_ARRIVE_LEVEL,
								"Agent has returned to boundary: re-inserting later");
					}
					break randomLoop;
				}
				/*
				 * If the agent has collided with another boundary, TODO
				 */
				if ( ! bndries.isEmpty() )
				{
					// FIXME Assume the boundary is solid for now
					this._agents.addAgent(anAgent);
					if ( Log.shouldWrite(AGENT_ARRIVE_LEVEL) )
					{
						Log.out(AGENT_ARRIVE_LEVEL,
								"Agent has hit another boundary");
					}
					break randomLoop;
				}
				/*
				 * The agent has not collided with any boundaries, so see if it
				 * has collided with any other agents.
				 */
				nbhAgents = this._agents.treeSearch(anAgent, pull);
				/*
				 * If the agent has collided with others, add it to the agent
				 * container and continue to the next agent.
				 */
				if ( ! nbhAgents.isEmpty() )
				{
					// TODO use the pulling method in Collision?
					this._agents.addAgent(anAgent);
					if ( Log.shouldWrite(AGENT_ARRIVE_LEVEL) )
					{
					Log.out(AGENT_ARRIVE_LEVEL,
							"Agent has hit another agent");
					}
					break randomLoop;
				}
				/*
				 * Ask the agent to move in a random walk.
				 */
				anAgent.event(STOCHASTIC_MOVE, MOVE_TSTEP);
			}
		}
		this.clearArrivalsLounge();
	}

	@Override
	public Collection<Agent> agentsToGrab()
	{
		List<Agent> out = new LinkedList<Agent>();
		/*
		 * Find all agents who are less than layerThickness away.
		 */
		Log.out(AGENT_LEVEL, "Grabbing all agents within layer thickness "+
				this._layerThickness);
		out.addAll(this._agents.treeSearch(this, this._layerThickness));
		/*
		 * Find all agents who are unattached to others or to a boundary,
		 * and who are on this side of the biofilm (in, e.g., the case of a
		 * floating granule).
		 */
		// TODO
		return out;
	}
}
