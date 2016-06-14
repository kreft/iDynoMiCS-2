/**
 * 
 */
package boundary.library;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmMembraneLiquid;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

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
	
	@Override
	public void updateConcentrations(EnvironmentContainer environment)
	{
		// TODO Auto-generated method stub
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
					"Unexpected: agents arriving from a membrane!");
		}
		super.agentsArrive(agentCont);
	}
}
