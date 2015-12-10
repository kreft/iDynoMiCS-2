package boundary;

import grid.GridBoundary.ConstantDirichlet;

public class BoundaryFixed extends BoundaryExternal
{
	public BoundaryFixed()
	{
		this._defaultGridMethod = new ConstantDirichlet();
	}
	
	public BoundaryFixed(double value)
	{
		this._defaultGridMethod = new ConstantDirichlet();
	}
}