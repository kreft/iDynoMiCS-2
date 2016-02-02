package surface;

public class Link {
	

	protected Point[] _points;
	
	protected Surface[] _surfaces;
	
	protected double _snap;
	
	public Link(Point[] points, Surface[] surfaces, double snapDistance)
	{
		this._points = points;
		this._surfaces = surfaces;
		this._snap = snapDistance;
	}
	
	public boolean evaluate(Collision collisionDomain)
	{
		double d = collisionDomain.sphereSphere((Sphere) _surfaces[0], (Sphere) _surfaces[1]);
		if (d < 0)
			return false;
		else if (d > _snap)
			return true;
		else
		{
			collisionDomain.pull(_surfaces[0], _surfaces[1]);
			return false;
		}
	}
}
