package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import shape.Dimension.DimName;

/**
 * \brief Boundary that allows neither agents nor solutes to cross it.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class SolidBoundary extends SpatialBoundary
{
	public SolidBoundary()
	{
		super();
	}
	
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
		this._detachability = 0.0;
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
	protected double calcDiffusiveFlow(SpatialGrid grid)
	{
		/*
		 * No matter what the concentration of the grid voxel, there is no
		 * diffusive flux across this boundary.
		 */
		return 0.0;
	}
	
	@Override
	public void updateWellMixedArray()
	{
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
					"Unexpected: agents arriving at a solid boundary!");
		}
		this.placeAgentsRandom();
		this.clearArrivalsLounge();
	}
}
