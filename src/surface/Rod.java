package surface;

import dataIO.ObjectFactory;
import generalInterfaces.HasBoundingBox;
import surface.BoundingBox;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Rod extends Surface implements HasBoundingBox {
	
    public Point[] _points;
	
    /**
     * Rest length of internal springs connecting the points.
     */
    public double _length;
	
	/**
	 * TODO
	 */
    public double _radius;
    
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
	
	public Rod(double[] pointA, double[] pointB, double radius)
	{
		this._points = new Point[] { new Point(pointA), new Point(pointB)};
		this._radius = radius;
	}
	
    public Rod(Point[] points)
    {
    	this._points = points;
    }
	

	public Rod(Rod rod) 
	{
		this._points = new Point[] {(Point) rod._points[0].copy(), 
				(Point) rod._points[1].copy()};
		this._length = (double) ObjectFactory.copy(rod._length);
		this._radius = (double) ObjectFactory.copy(rod._radius);
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

	public double[][] pointMatrix()
	{
		double[][] p = new double[_points[0].nDim()][_points.length];
		for(int i = 0; i < _points.length; i++)
			p[i] = _points[i].getPosition();
		return p;
	}
	
	public BoundingBox boundingBox(double margin)
	{
		return new BoundingBox(pointMatrix(),_radius,margin);
	}

	public BoundingBox boundingBox()
	{
		return new BoundingBox(pointMatrix(),_radius);
	}
}