package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.library.GasToMembrane;
import grid.SpatialGrid;
import idynomics.EnvironmentContainer;
import shape.Dimension.DimName;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class BiofilmMembraneGas extends SpatialBoundary
{
	/**
	 * \brief Construct a membrane by giving it the information it needs about
	 * its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public BiofilmMembraneGas(DimName dim, int extreme)
	{
		super(dim, extreme);
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
	{
		return GasToMembrane.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public double getFlux(SpatialGrid grid)
	{
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public void updateConcentrations(EnvironmentContainer environment)
	{

	}

}
