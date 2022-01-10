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

	@Override
	public void additionalPartnerUpdate() {}
}
