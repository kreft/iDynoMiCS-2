package shape.resolution;

public class UniformResolution extends ResolutionCalculator
{
	@Override
	public void init(double targetResolution, double min, double max)
	{
		this._min = min;
		this._max = max;
		this._targetRes = targetResolution;
		this._nVoxel = (int) (getTotalLength() / targetResolution);
		this._resolution = getTotalLength() / this._nVoxel;
		double altRes = getTotalLength() / (this._nVoxel + 1);
		if ( isAltResBetter(this._resolution, altRes, targetResolution) )
		{
			this._nVoxel++;
			this._resolution = altRes;
		}
	}
}
