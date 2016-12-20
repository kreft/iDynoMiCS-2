/**
 * 
 */
package shape;

import shape.resolution.ResolutionCalculator;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public final class ShapeConventions
{

	/**
	 * \brief Dummy resolution calculator that always has exactly one voxel.
	 */
	// NOTE exploratory work, may not be used
	public static class SingleVoxel extends ResolutionCalculator
	{
		public SingleVoxel()
		{
			this._nVoxel = 1;
			this._resolution = 1.0;
			this._targetRes = 1.0;
			this._min = 0.0;
			this._max = 1.0;
		}
		
		@Override
		public void init(double targetResolution, double min, double max)
		{
			this._nVoxel = 1;
			this._targetRes = targetResolution;
			this._resolution = targetResolution;
			this._min = min;
			this._max = max;
		}
		
		@Override
		public void setResolution(double targetResolution)
		{
			this._resolution = targetResolution;
		}

		@Override
		public double getCumulativeResolution(int voxelIndex)
		{
			if (voxelIndex <= 0)
				return this._min;
			return this._max;
		}

		@Override
		public int getVoxelIndex(double location)
		{
			return 0;
		}
	}
}
