/**
 * 
 */
package boundary.library;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmMembraneLiquid;
import dataIO.Log;
import dataIO.Log.Tier;

/**
 * \brief TODO
 * 
 */
public class ChemostatToMembrane extends Boundary
{
	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/
	
	@Override
	protected Class<?> getPartnerClass()
	{
		return BiofilmMembraneLiquid.class;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/
	
	@Override
	public void agentsArrive()
	{
		if ( ! this._arrivalsLounge.isEmpty() )
		{
			Log.out(Tier.NORMAL,
					"Unexpected: agents arriving from a membrane!");
		}
		super.agentsArrive();
	}
}
