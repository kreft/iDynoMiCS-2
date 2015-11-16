package boundary;

public class BoundaryCyclic extends BoundaryConnected
{
	public BoundaryCyclic()
	{
		super();
		this._defaultGridMethod = Boundary.cyclic(); 
	}
}