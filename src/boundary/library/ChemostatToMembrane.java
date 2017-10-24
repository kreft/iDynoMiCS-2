/**
 * 
 */
package boundary.library;

import org.w3c.dom.Element;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmMembraneLiquid;
import dataIO.Log;
import dataIO.Log.Tier;
import settable.Settable;

/**
 * \brief TODO
 * 
 */
public class ChemostatToMembrane extends Boundary
{
	public ChemostatToMembrane()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		// TODO Auto-generated method stub
		
	}
	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/
	
	@Override
	public Class<?> getPartnerClass()
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
