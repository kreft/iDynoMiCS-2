/**
 * 
 */
package boundary.library;

import org.w3c.dom.Element;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmMembraneGas;
import settable.Settable;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class GasToMembrane extends Boundary
{
	public GasToMembrane()
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
	protected Class<?> getPartnerClass()
	{
		return BiofilmMembraneGas.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
}
