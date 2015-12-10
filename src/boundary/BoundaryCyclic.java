package boundary;

import grid.GridBoundary;

public class BoundaryCyclic extends BoundaryConnected
{
	public BoundaryCyclic()
	{
		super();
		this._defaultGridMethod = GridBoundary.cyclic(); 
	}
}