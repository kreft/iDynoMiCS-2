/**
 * 
 */
package boundary.library;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import idynomics.EnvironmentContainer;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToBoundaryLayer extends Boundary
{
	@Override
	public Class<?> getPartnerClass()
	{
		return BiofilmBoundaryLayer.class;
	}
	
	/*************************************************************************
	 * SOLUTE TRANSFERS
	 ************************************************************************/
	
	@Override
	public void updateConcentrations(EnvironmentContainer environment)
	{
		double chemoConcn, biofilmConcn;
		for ( String name : environment.getSoluteNames() )
		{
			/* */
			biofilmConcn = this._partner.getConcentration(name);
			this.setConcentration(name, biofilmConcn);
			/* */
			chemoConcn = environment.getAverageConcentration(name);
			this._partner.setConcentration(name, chemoConcn);
		}
	}
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	// TODO [Rob 13June2016]: We need to grab agents from the chemostat here,
	// in a similar way to ChemostatToChemostat, but there is no "flow rate".
}
