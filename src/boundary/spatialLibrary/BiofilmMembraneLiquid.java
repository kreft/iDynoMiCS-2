package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.library.ChemostatToMembrane;
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
public class BiofilmMembraneLiquid extends SpatialBoundary
{
	/**
	 * \brief Construct a membrane by giving it the information it needs about
	 * its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public BiofilmMembraneLiquid(DimName dim, int extreme)
	{
		super(dim, extreme);
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
	{
		return ChemostatToMembrane.class;
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
		return 0;
	}
	
	@Override
	public boolean needsToUpdateWellMixed()
	{
		// TODO check this
		return false;
	}
	
	@Override
	public void updateWellMixedArray(SpatialGrid grid, AgentContainer agents)
	{
		// TODO default method used for now, check this is appropriate
		this.setWellMixedByDistance(grid);
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	@Override
	public void agentsArrive(AgentContainer agentCont)
	{
		if ( ! this._arrivalsLounge.isEmpty() )
		{
			Log.out(Tier.NORMAL,
					"Unexpected: agents arriving at a membrane!");
		}
		this.placeAgentsRandom(agentCont);
		this.clearArrivalsLoungue();
	}
}
