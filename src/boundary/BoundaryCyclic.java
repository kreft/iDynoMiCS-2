package boundary;

import grid.GridBoundary.Cyclic;

public class BoundaryCyclic extends BoundaryConnected
{
	public BoundaryCyclic()
	{
		super();
		this._defaultGridMethod = new Cyclic(); 
	}
}