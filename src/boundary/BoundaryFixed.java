package boundary;

public class BoundaryFixed extends BoundaryExternal
{
	public BoundaryFixed()
	{
		this._defaultGridMethod = constantDirichlet(0.0);
	}
	
	public BoundaryFixed(double value)
	{
		this._defaultGridMethod = constantDirichlet(value);
	}
}