/**
 * 
 */
package boundary.library;

import org.w3c.dom.Element;

import boundary.Boundary;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import settable.Settable;

/**
 * \brief Boundary connecting a dimensionless compartment to a compartment
 * this spatial structure.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToBoundaryLayer extends Boundary
{
	public ChemostatToBoundaryLayer()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		// TODO Auto-generated method stub
		
	}

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
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	// TODO [Rob 13June2016]: We need to grab agents from the chemostat here,
	// in a similar way to ChemostatToChemostat, but there is no "flow rate".
}
