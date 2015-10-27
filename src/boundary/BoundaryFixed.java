package boundary;

public class BoundaryFixed extends BoundaryExternal
{
	public BoundaryFixed()
	{
		this._gridMethod = dirichlet(0.0);
	}
	
	public BoundaryFixed(double value)
	{
		this._gridMethod = dirichlet(value);
	}
}