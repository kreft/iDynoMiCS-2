package shape.resolution;

import utility.ExtraMath;

public class MultigridResolution extends ResolutionCalculator
{
	@Override
	public void init(double targetResolution, double min, double max)
	{
		this._min = min; 
		this._max = max;
		this._targetRes = targetResolution;
		/* Single-voxel test to start with. */
		this._nVoxel = 1;
		this._resolution = targetResolution;
		/* Variables to test splitting the grid into more voxels. */
		int exponent = 1;
		int altNVoxel = 2;
		double altRes = this.getTotalLength() / altNVoxel;
		/* Testing loop. */
		while( isAltResBetter(this._resolution, altRes, targetResolution) )
		{
			this._nVoxel = altNVoxel;
			exponent++;
			altNVoxel = ExtraMath.exp2(exponent);
			this._resolution = altRes;
			altRes = this.getTotalLength() / altNVoxel;
		}
	}
}
