package boundary;

import boundary.grid.GridMethodLibrary.ZeroFlux;

public class BoundaryZeroFlux extends BoundaryExternal
{

	public BoundaryZeroFlux()
	{
		this._defaultGridMethod = new ZeroFlux();
	}
}
