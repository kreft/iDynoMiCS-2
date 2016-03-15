package boundary;

import boundary.grid.GridMethodLibrary.ConstantDirichlet;

public class BoundaryFixed extends Boundary
{
	public BoundaryFixed()
	{
		this._defaultGridMethod = new ConstantDirichlet();
	}
	
	public BoundaryFixed(double value)
	{
		ConstantDirichlet gm = new ConstantDirichlet();
		gm.setValue(value);
		this._defaultGridMethod = gm;
		
	}
}