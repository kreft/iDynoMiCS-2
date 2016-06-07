package boundary.spatialLibrary;

import java.util.HashMap;
import java.util.Map;

import boundary.SpatialBoundary;
import shape.Dimension.DimName;

/**
 * \brief Spatial boundary where solute concentrations are kept fixed. Solid
 * surface to agents. Intended for testing purposes.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class FixedBoundary extends SpatialBoundary
{
	/**
	 * Solute concentrations.
	 */
	private Map<String,Double> _concns = new HashMap<String,Double>();
	
	/**
	 * \brief TODO
	 * 
	 * @param dim
	 * @param extreme
	 */
	public FixedBoundary(DimName dim, int extreme)
	{
		super(dim, extreme);
	}
	
	/**
	 * \brief Set the concentration of a solute at this boundary.
	 * 
	 * @param name Name of the solute.
	 * @param value Concentration of the solute.
	 */
	public void setConcn(String name, double value)
	{
		this._concns.put(name, value);
	}
}
