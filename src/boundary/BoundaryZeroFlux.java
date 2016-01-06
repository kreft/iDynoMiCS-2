package boundary;

import grid.GridBoundary.ZeroFlux;;

public class BoundaryZeroFlux extends BoundaryExternal
{

	public BoundaryZeroFlux()
	{
		this._defaultGridMethod = new ZeroFlux();
	}
}
