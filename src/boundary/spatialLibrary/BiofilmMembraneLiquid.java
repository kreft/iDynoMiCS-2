package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.library.ChemostatToMembrane;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
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
		this._detachability = 0.0;
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
	protected double getDetachability()
	{
		return 0.0;
	}
	
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
