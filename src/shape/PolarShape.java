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
		//TODO: does sometimes start a bit too early (?) (possibly rounding...)
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
		//double cur_max = rC.getCumulativeResolution(this._currentCoord[1]);
		rC = this.getResolutionCalculator(this._currentNeighbor, 1);
		int new_index = rC.getVoxelIndex(cur_min);
		// FIXME is this really necessary?
//		double nbh_max = rC.getCumulativeResolution(new_index);
//		
//		/* if we do not overlap 'enough', try to take next */
//		double th = getCurrentArcLengthDiffCutoff();
//		if (cur_min >= nbh_max - th)
//		{
//			/* nbh_max is actually nbh_min now */
//			if (cur_max - th >= nbh_max)
//				new_index++;
//			else 
//				return false;
//		}
		this._currentNeighbor[1] = new_index;
		return true;
	}
}
