package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.library.ChemostatToMembrane;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class BiofilmMembraneLiquid extends SpatialBoundary
{
	public BiofilmMembraneLiquid()
	{
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
	protected double calcDiffusiveFlow(SpatialGrid grid)
	{
		// TODO
		return 0;
	}
	
	@Override
	public void updateWellMixedArray()
	{
		// TODO default method used for now, check this is appropriate
		this.setWellMixedByDistance();
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	@Override
	public void agentsArrive()
	{
		if ( ! this._arrivalsLounge.isEmpty() )
		{
			Log.out(Tier.NORMAL,
					"Unexpected: agents arriving at a membrane!");
		}
		this.placeAgentsRandom();
		this.clearArrivalsLounge();
	}
}
