/**
 * 
 */
package boundary.library;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmBoundaryLayer;

/**
 * \brief Boundary connecting a dimensionless compartment to a compartment
 * this spatial structure.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToBoundaryLayer extends Boundary
{
	/* ************************************************************************
	 * PARTNER BOUNDARY
	 * ***********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
	{
		return BiofilmBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void updateConcentrations()
	{
		double chemoConcn, biofilmConcn;
		for ( String name : this._environment.getSoluteNames() )
		{
			/* */
			biofilmConcn = this._partner.getConcentration(name);
			this.setConcentration(name, biofilmConcn);
			/* */
			chemoConcn = this._environment.getAverageConcentration(name);
			this._partner.setConcentration(name, chemoConcn);
		}
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	// TODO [Rob 13June2016]: We need to grab agents from the chemostat here,
	// in a similar way to ChemostatToChemostat, but there is no "flow rate".
}
