package shape.resolution;

import shape.Dimension;

public class UniformResolution extends ResolutionCalculator
{
	public UniformResolution()
	{
		super();
	}
	
	public UniformResolution(Dimension dimension)
	{
		super(dimension);
	}
	
	@Override
	protected void init(double targetResolution, double min, double max)
	{
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
