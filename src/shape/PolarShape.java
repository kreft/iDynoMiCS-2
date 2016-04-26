package shape;

import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;

public abstract class PolarShape extends Shape
{
	/**
	 * \brief Used to move neighbor iterator into a new shell.
	 * 
	 * <p>May change first and second coordinates, while leaving third
	 * unchanged.</p>
	 * 
	 * @param shellIndex Index of the shell you want to move the neighbor
	 * iterator into.
	 * @return True is this was successful, false if it was not.
	 */
	protected boolean setNbhFirstInNewShell(int shellIndex)
	{
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[0] = shellIndex;
		/*
		 * First check that the new shell is inside the grid. If we're on a
		 * defined boundary, the angular coordinate is irrelevant.
		 */
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 0);
		if ( this.isOnUndefinedBoundary(this._currentNeighbor, DimName.R) )
			return false;
		if ( this.isOnBoundary(this._currentNeighbor, 0))
			return true;
		/*
		 * We're on an intermediate shell, so find the voxel which has the
		 * current coordinate's minimum angle inside it.
		 */
		rC = this.getResolutionCalculator(this._currentCoord, 1);
		double cur_min = rC.getCumulativeResolution(this._currentCoord[1] - 1);
		rC = this.getResolutionCalculator(this._currentNeighbor, 1);
		int new_index = rC.getVoxelIndex(cur_min);
		this._currentNeighbor[1] = new_index;
		return true;
	}
	
	/**
	 * \brief Try to increase the neighbor iterator by one in the given
	 * dimension.
	 * 
	 * @param dim Name of the dimension required.
	 * @return True is this was successful, false if it was not.
	 */
	protected boolean increaseNbhByOnePolar(DimName dim)
	{
		int index = this.getDimensionIndex(dim);
		Dimension dimension = this.getDimension(dim);
		ResCalc rC = this.getResolutionCalculator(this._currentNeighbor, index);
		/* If we are already on the maximum boundary, we cannot go further. */
		if ( this._currentNeighbor[index] > rC.getNVoxel() - 1 )
				return false;
		/* Do not allow the neighbor to be on an undefined maximum boundary. */
		if ( this._currentNeighbor[index] == rC.getNVoxel() - 1 )
			if ( ! dimension.isBoundaryDefined(1) )
				return false;
		/*
		 * If increasing would mean we no longer overlap, report failure.
		 */
		double nbhMax = rC.getCumulativeResolution(this._currentNeighbor[index]);
		rC = this.getResolutionCalculator(this._currentCoord, index);
		double curMax = rC.getCumulativeResolution(this._currentCoord[index]);
		if ( nbhMax >= curMax )
			return false;
		/*
		 * All checks have passed, so increase and report success.
		 */
		this._currentNeighbor[index]++;
		return true;
	}
}
