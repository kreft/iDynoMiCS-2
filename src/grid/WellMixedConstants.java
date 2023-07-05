package grid;

import idynomics.Idynomics;

/**
 * Constants for the {@code WELLMIXED} array type.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 *
 */
public final class WellMixedConstants
{
	/**
	 * Not mixed at all, i.e. dominated by diffusion.
	 */
	public final static double NOT_MIXED = 0.0;
	
	/**
	 * Completely well-mixed, i.e. the effect of diffusion is neglible.
	 */
	public final static double COMPLETELY_MIXED = 1.0;
	
	/**
	 * 
	 * @param commonGrid
	 * @param coord
	 * @return
	 */
	public final static boolean isWellMixed(SpatialGrid commonGrid, int[] coord)
	{
		if (commonGrid == null)
			return false;
		
		return commonGrid.getValueAt(ArrayType.WELLMIXED, coord) >= 
				Idynomics.global.relativeThresholdWellMixedness * 
				COMPLETELY_MIXED;
	}
}
