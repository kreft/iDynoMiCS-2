package surface;

import utility.Copier;
import agent.body.Body;


public class Sphere extends Surface{
	
    public Point _point;

    private double _radius;
    
    private Body body;
    
    public Sphere(Point point, double radius)
    {
    	this._point = point;
    	this._radius = radius;
    }
    
    public Sphere(Point point, Body body)
    {
    	this._point = point;
    	this.body = body;
    }

    /**
     * copy constructor
     * @param sphere
     */
	public Sphere(Sphere sphere) {
		this._point = (Point) Copier.copy(sphere._point);
		this._radius = (double) Copier.copy(sphere._radius);
		this.body = sphere.body; // We do not want to create a new body
	}

	public Type type() {
		return Surface.Type.SPHERE;
	}
	
	public double getRadius()
	{
		if (body == null)
			return _radius;
		return body.getRadius();
	}
	
	public void setBody(Body body)
	{
		this.body = body;
	}
	
	//TODO
	public BoundingBox boundingBox()
	{
		return null;
	}

}