package boundary;

import boundary.grid.GridBoundaryLibrary;
import boundary.grid.GridBoundaryLibrary.ConstantDirichlet;

public class BoundaryFixed extends BoundaryExternal
{
	public BoundaryFixed()
	{
		this._defaultGridMethod = new GridBoundaryLibrary.ConstantDirichlet();
	}
	
	public BoundaryFixed(double value)
	{
		GridBoundaryLibrary.ConstantDirichlet gm = new GridBoundaryLibrary.ConstantDirichlet();
		gm.setValue(value);
		this._defaultGridMethod = gm;
		
	}
}