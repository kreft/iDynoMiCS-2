package boundary;

import java.util.HashMap;
import java.util.Map;

/**
 * \brief A well-mixed boundary is a spatial boundary that <i>must</i> have a
 * well-mixed region associated with it.
 * 
 * <p>Since other spatial boundaries have methods to deal with well-mixed
 * regions if one of these is present in the compartment, this is a small
 * increment on the structure of SpatialBoundary.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class WellMixedBoundary extends SpatialBoundary
{
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();
	
	
	public WellMixedBoundary()
	{
		super();
	}
	
	@Override
	public boolean needsToUpdateWellMixed()
	{
		return true;
	}
	
	/**
	 * @param soluteName Name of the solute required.
	 * @return Concentration of this solute at this well-mixed boundary.
	 */
	public double getConcentration(String soluteName)
	{
		return this._concns.get(soluteName);
	}
}
