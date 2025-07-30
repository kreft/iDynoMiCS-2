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
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 */
	
	public ChemostatToBoundaryLayer()
	{
		super();
	}

	/* ************************************************************************
	 * PARTNER BOUNDARY
	 * ***********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return BiofilmBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override 
	public void additionalPartnerUpdate()
	{
		this._partner.additionalPartnerUpdate();
	}
	
	public double getSoluteConcentration(String soluteName)
	{
		return this._environment.getAverageConcentration(soluteName);
	}

}
