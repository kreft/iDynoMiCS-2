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
	public static class SingleVoxel extends ResolutionCalculator
	{
		public SingleVoxel(Dimension dimension)
		{
			super(dimension);
			this._nVoxel = 1;
			this._resolution = 1.0;
			this._targetRes = 1.0;
		}
		
		@Override
		protected void init(double targetResolution, double min, double max)
		{
			this._nVoxel = 1;
			this._targetRes = targetResolution;
			this._resolution = targetResolution;
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
				return this._dimension.getExtreme(0);
			return this._dimension.getExtreme(1);
		}

		@Override
		public int getVoxelIndex(double location)
		{
			return 0;
		}
	}
}
