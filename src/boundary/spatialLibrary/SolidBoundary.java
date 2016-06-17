package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import shape.Dimension.DimName;

/**
 * \brief Boundary that allows neither agents nor solutes to cross it.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class SolidBoundary extends SpatialBoundary
{
	/**
	 * \brief Construct a solid boundary by giving it the information it
	 * needs about its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public SolidBoundary(DimName dim, int extreme)
	{
		super(dim, extreme);
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
	{
		/* 
		 * This boundary shouldn't really have a partner, but if one is
		 * requested then just return another solid boundary.
		 */
		return SolidBoundary.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void updateConcentrations(EnvironmentContainer environment)
	{
		/* Do nothing! */
	}

	@Override
	public double getFlux(SpatialGrid grid)
	{
		/*
		 * No matter what the concentration of the grid voxel, there is no
		 * diffusive flux across this boundary.
		 */
		return 0.0;
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
					"Unexpected: agents arriving at a solid boundary!");
		}
		this.placeAgentsRandom(agentCont);
		this.clearArrivalsLoungue();
	}
}
