package surface;

import utility.Copier;
import agent.Body;


public class Rod extends Surface{
	
    public Point[] _points;
	
    /**
     * Rest length of internal springs connecting the points.
     */
    private double _length;
	
	/**
	 * 
	 */
    private double _radius;
    
    /**
     * The body this Rod belongs to (null if none)
     */
    private Body body;
    
    public Rod(Point[] points, double spineLength, double radius)
    {
    	this._points = points;
    	this._length = spineLength;
    	this._radius = radius;
    }

	public Rod(Point pointA, Point pointB, double spineLength, double radius)
	{
		this._points = new Point[] { pointA , pointB };
		this._length = spineLength;
		this._radius = radius;
    }
	
    public Rod(Point[] points, Body body)
    {
    	this._points = points;
    	this.body = body;
    }
	

	public Rod(Rod rod) 
	{
		this._points = new Point[] {(Point) rod._points[0].copy(), 
				(Point) rod._points[0].copy()};
		this._length = (double) Copier.copy(rod._length);
		this._radius = (double) Copier.copy(rod._radius);
		this.body = rod.body;
	}


	public Type type() {
		return Surface.Type.ROD;

	}
	
	public double getRadius()
	{
		if (body == null)
			return _radius;
		return body.getRadius();
	}
	
	public double getLength()
	{
		if (body == null)
			return _length;
		return body.getLength();
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