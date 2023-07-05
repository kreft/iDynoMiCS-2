package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.library.ChemostatToMembrane;
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
		super();
	}

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	@Override
	protected boolean needsLayerThickness()
	{
		// TODO check this!
		return true;
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
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

	@Override
	public void additionalPartnerUpdate() {}
	
	public boolean isSolid()
	{
		return true;
	}
}
