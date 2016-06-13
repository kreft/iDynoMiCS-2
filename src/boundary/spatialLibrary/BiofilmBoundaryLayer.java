package boundary.spatialLibrary;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
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
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class BiofilmBoundaryLayer extends SpatialBoundary
{
	/**
	 * Boundary layer thickness.
	 */
	// TODO set this from protocol file
	private double _layerTh = 10.0;

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
		boolean hasCollided = false;
		for ( Agent anAgent : this._arrivalsLounge )
		{
			Log.out(Tier.DEBUG, "Moving agent (UID: "+anAgent.identity()+
					") to top of boundary layer");
			// NOTE Rob [19/5/2016]: this loop is work in progress.
			while ( ! hasCollided )
			{
				nbhAgents = agentCont.treeSearch(anAgent, this._layerTh);
				if ( ! nbhAgents.isEmpty() )
				{
					hasCollided = true;
					break;
				}
				bndries = agentCont.boundarySearch(anAgent, this._layerTh);
				if ( ! bndries.isEmpty() )
				{
					// FIXME stopping is a temporary fix: we need to apply
					// the boundary here
					hasCollided = true;
					break;
				}
				agentCont.moveAlongDimension(anAgent, this._dim, dist);
			}
			// TODO ask the agent to move now?
		}
		this.clearArrivalsLoungue();
	}

	@Override
	public List<Agent> agentsToGrab(AgentContainer agentCont, double timeStep)
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
