package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import grid.SpatialGrid;

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

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	@Override
	protected boolean needsLayerThickness()
	{
		return false;
	}
	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return null;
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

	@Override
	public void additionalPartnerUpdate() {}
	
	public boolean isSolid()
	{
		return true;
	}
}
