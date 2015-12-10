package boundary;

import grid.GridBoundary;

public class BoundaryFixed extends BoundaryExternal
{
	public BoundaryFixed()
	{
		this._defaultGridMethod = GridBoundary.constantDirichlet(0.0);
	}
	
	public BoundaryFixed(double value)
	{
		this._defaultGridMethod = GridBoundary.constantDirichlet(value);
	}
}