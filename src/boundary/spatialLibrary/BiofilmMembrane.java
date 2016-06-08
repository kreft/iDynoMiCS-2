/**
 * 
 */
package boundary.spatialLibrary;

import boundary.SpatialBoundary;
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
public class BiofilmMembrane extends SpatialBoundary
{

	/**\brief TODO
	 * 
	 * @param dim
	 * @param extreme
	 */
	public BiofilmMembrane(DimName dim, int extreme)
	{
		super(dim, extreme);
	}
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	@Override
	public void agentsArrive(AgentContainer agentCont)
	{
		Log.out(Tier.NORMAL,
				"Unexpected: agents arriving at a membrane!");
		this.placeAgentsRandom(agentCont);
	}

	@Override
	public double getFlux(SpatialGrid grid)
	{
		// TODO
		return 0;
	}
}
