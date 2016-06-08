package boundary.spatialLibrary;

import java.util.HashMap;
import java.util.Map;

import boundary.SpatialBoundary;
import grid.ArrayType;
import grid.SpatialGrid;
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
	
	@Override
	public double getFlux(SpatialGrid grid)
	{
		/* The difference in concentration is the same as in SpatialGrid. */
		double concnDiff = this._concns.get(grid.getName()) -
				grid.getValueAtCurrent(ArrayType.CONCN);
		/* The diffusivity comes only from the current voxel. */
		double diffusivity = grid.getValueAtCurrent(ArrayType.DIFFUSIVITY);
		/* Shape handles the shared surface area on a boundary. */
		double sArea = grid.getShape().nbhCurrSharedArea();
		/* Shape handles the centre-centre distance on a boundary. */
		double dist = grid.getShape().nbhCurrDistance();
		/* The current iterator voxel volume is the same as in SpatialGrid. */
		double vol = grid.getShape().getCurrVoxelVolume();
		double flux = concnDiff * diffusivity * sArea / ( dist * vol );
		return flux;
	}
}
