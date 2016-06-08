/**
 * 
 */
package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.grid.GridMethodLibrary.ZeroFlux;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import shape.Dimension.DimName;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SolidBoundary extends SpatialBoundary
{
	/**\brief TODO
	 * 
	 * @param dim
	 * @param extreme
	 */
	public SolidBoundary(DimName dim, int extreme)
	{
		super(dim, extreme);
		this._defaultGridMethod = new ZeroFlux();
	}

	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	@Override
	public void agentsArrive(AgentContainer agentCont)
	{
		Log.out(Tier.NORMAL,
				"Unexpected: agents arriving at a solid boundary!");
		this.placeAgentsRandom(agentCont);
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
}
