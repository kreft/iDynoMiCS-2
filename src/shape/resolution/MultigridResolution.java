package shape.resolution;

import shape.Dimension;
import shape.ShapeConventions.SingleVoxel;
import utility.ExtraMath;

/**
 * TODO description and author information
 * @author
 *
 */
public class MultigridResolution extends ResolutionCalculator
{
	public MultigridResolution()
	{
		super();
	}
	
	public MultigridResolution(Dimension dimension)
	{
		super(dimension);
	}
	
	@Override
	protected void init(double targetResolution, double min, double max)
	{
		this._targetRes = targetResolution;
		/* Single-voxel test to start with. */
		this._nVoxel = 1;
		this._resolution = this.getTotalLength();
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
			MultigridResolution out = new MultigridResolution(this._dimension);
			out._targetRes = 2.0 * this._targetRes;
			out._nVoxel = (int)(0.5 * this._nVoxel);
			out._resolution = 2.0 * this._resolution;
			return out;
		}
		else
		{
			SingleVoxel sV = new SingleVoxel(this._dimension);
			sV.setResolution(this.getTotalLength());
			return sV;
		}
	}

}
