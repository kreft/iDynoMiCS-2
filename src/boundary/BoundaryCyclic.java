package boundary;

import grid.SpatialGrid.GridMethod;

public class BoundaryCyclic extends BoundaryConnected
{
	public BoundaryCyclic()
	{
		super();
		this._gridMethod = new GridMethod()
				{

					@Override
					public int[] getCorrectCoord(int[] coord)
					{
						// TODO
						return null;
					}
				};
	}
	
	
}