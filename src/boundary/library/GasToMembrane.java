/**
 * 
 */
package boundary.library;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmMembraneGas;
import idynomics.EnvironmentContainer;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class GasToMembrane extends Boundary
{
	
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
	
	@Override
	public void updateConcentrations(EnvironmentContainer environment)
	{
		
	}

}
