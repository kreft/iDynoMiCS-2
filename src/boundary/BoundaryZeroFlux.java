package boundary;

public class BoundaryZeroFlux extends BoundaryExternal
{

	public BoundaryZeroFlux()
	{
		this._defaultGridMethod = zeroFlux();
	}
}
