package shape.resolution;

import shape.Dimension;
import shape.ShapeConventions.SingleVoxel;
import utility.ExtraMath;

/**
 * TODO description and author information
 * @author Stefan Lang @ Robert Clegg?
 *
 */
public class MgFASResolution extends ResolutionCalculator
{
	public MgFASResolution()
	{
		super();
	}

	public MgFASResolution(Dimension dimension)
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
			altRes = this.getTotalLength() / (altNVoxel);
		}
		this._nVoxel++;
	}

}
