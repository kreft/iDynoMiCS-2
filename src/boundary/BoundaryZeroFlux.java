package boundary;

import boundary.grid.GridBoundaryLibrary.ZeroFlux;

public class BoundaryZeroFlux extends BoundaryExternal
{

	public BoundaryZeroFlux()
	{
		this._defaultGridMethod = new ZeroFlux();
	}
}
