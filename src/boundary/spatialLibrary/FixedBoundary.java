package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import shape.Dimension.DimName;

/**
 * \brief Spatial boundary where solute concentrations are kept fixed. Solid
 * surface to agents. Intended for testing purposes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class FixedBoundary extends SpatialBoundary
{
	/**
	 * \brief Construct a fixed boundary by giving it the information it
	 * needs about its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public FixedBoundary(DimName dim, int extreme)
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
		 * requested then just return another fixed boundary.
		 */
		return FixedBoundary.class;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	@Override
	public void updateConcentrations()
	{
		/* Do nothing! */
	}
	
	@Override
	public double getFlow(SpatialGrid grid)
	{
		Tier level = Tier.BULK;
		/* The difference in concentration is the same as in SpatialGrid. */
		double concnDiff = this._concns.get(grid.getName()) -
				grid.getValueAtCurrent(ArrayType.CONCN);
		/* The diffusivity comes only from the current voxel. */
		double diffusivity = grid.getValueAtCurrent(ArrayType.DIFFUSIVITY);
		/* Shape handles the shared surface area on a boundary. */
		double sArea = grid.getShape().nbhCurrSharedArea();
		/* Shape handles the centre-centre distance on a boundary. */
		double dist = grid.getShape().nbhCurrDistance();
		/* Calculate flux and flow in the same way as in SpatialGrid. */
		double flux = concnDiff * diffusivity / dist ;
		double flow = flux * sArea;
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "FixedBoundary flux for "+grid.getName()+":");
			Log.out(level, "  concn diff is "+concnDiff);
			Log.out(level, "  diffusivity is "+diffusivity);
			Log.out(level, "  distance is "+dist);
			Log.out(level, "  => flux = "+flux);
			Log.out(level, "  surface area is "+sArea);
			Log.out(level, "  => flow = "+flow);
		}
		return flow;
	}
	
	@Override
	public boolean needsToUpdateWellMixed()
	{
		return false;
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
	public void agentsArrive()
	{
		if ( ! this._arrivalsLounge.isEmpty() )
		{
			Log.out(Tier.NORMAL,
					"Unexpected: agents arriving at a fixed boundary!");
		}
		this.placeAgentsRandom();
		this.clearArrivalsLoungue();
	}
}
