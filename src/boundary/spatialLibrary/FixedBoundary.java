package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
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
	public void updateConcentrations(EnvironmentContainer environment)
	{
		/* Do nothing! */
	}
	
	@Override
	public double getFlux(SpatialGrid grid)
	{
		Tier level = Tier.BULK;
		Log.out(level, "FixedBoundary getting flux for "+grid.getName()+":");
		/* The difference in concentration is the same as in SpatialGrid. */
		double concnDiff = this._concns.get(grid.getName()) -
				grid.getValueAtCurrent(ArrayType.CONCN);
		Log.out(level, "  concn diff is "+concnDiff);
		/* The diffusivity comes only from the current voxel. */
		double diffusivity = grid.getValueAtCurrent(ArrayType.DIFFUSIVITY);
		Log.out(level, "  diffusivity is "+diffusivity);
		/* Shape handles the shared surface area on a boundary. */
		double sArea = grid.getShape().nbhCurrSharedArea();
		Log.out(level, "  surface area is "+sArea);
		/* Shape handles the centre-centre distance on a boundary. */
		double dist = grid.getShape().nbhCurrDistance();
		Log.out(level, "  distance is "+dist);
		/* The current iterator voxel volume is the same as in SpatialGrid. */
		double vol = grid.getShape().getCurrVoxelVolume();
		Log.out(level, "  volume is "+vol);
		double flux = concnDiff * diffusivity * sArea / ( dist * vol );
		Log.out(level, "  => flux = "+flux);
		return flux;
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
					"Unexpected: agents arriving at a fixed boundary!");
		}
		this.placeAgentsRandom(agentCont);
		this.clearArrivalsLoungue();
	}
}
