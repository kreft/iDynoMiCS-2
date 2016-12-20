package shape.resolution;

import shape.ShapeConventions.SingleVoxel;
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
	
	public ResolutionCalculator getCoarserResolution()
	{
		if ( this._nVoxel > 2 )
		{
			MultigridResolution out = new MultigridResolution();
			out._min = this._min;
			out._max = this._max;
			out._targetRes = 0.5 * this._targetRes;
			out._nVoxel = (int)(0.5 * this._nVoxel);
			out._resolution = 0.5 * this._resolution;
			return out;
		}
		else
		{
			SingleVoxel sV = new SingleVoxel();
			sV.init(this.getTotalLength(), this._min, this._max);
			return sV;
		}
	}
}
