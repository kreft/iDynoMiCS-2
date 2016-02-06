package surface;

import utility.Copier;
import agent.Body;

/**
 * 
 * @author baco
 *
 */
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
	
    public Rod(Point[] points)
    {
    	this._points = points;
    }
	

	public Rod(Rod rod) 
	{
		this._points = new Point[] {(Point) rod._points[0].copy(), 
				(Point) rod._points[0].copy()};
		this._length = (double) Copier.copy(rod._length);
		this._radius = (double) Copier.copy(rod._radius);
	}


	public Type type() {
		return Surface.Type.ROD;
	}
	
	public double getRadius()
	{
		return _radius;
	}
	
	public double getLength()
	{
		return _length;
	}
	
	public void set(double radius, double spineLength)
	{
		this._radius = radius;
		this._length = spineLength;
	}

	//TODO
	public BoundingBox boundingBox()
	{
		return null;
	}
}