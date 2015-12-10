package boundary;

import grid.GridBoundary;

public class BoundaryZeroFlux extends BoundaryExternal
{

	public BoundaryZeroFlux()
	{
		this._defaultGridMethod = GridBoundary.zeroFlux();
	}
}
