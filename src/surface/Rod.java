package surface;

import dataIO.ObjectFactory;
import generalInterfaces.HasBoundingBox;
import shape.Shape;
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
	
	public void setRadius(double radius)
	{
		this._radius = radius;
	}
	
	public void setLength(double spineLength)
	{
		this._length = spineLength;
	}

	public double[][] pointMatrix(Shape shape)
	{
		double[][] p = new double[_points.length][_points[0].nDim()];
		for(int i = 0; i < _points.length; i++)
			if ( i < 1)
				p[i] = _points[i].getPosition();
			else
				p[i] = shape.getNearestShadowPoint( _points[i].getPosition(), 
						p[i-1]);
		return p;
	}
	
	@Override
	public int dimensions() 
	{
		return this._points[0].nDim();
	}
	

	/*
	 * FIXME rod bounding box is broken since it receives periodic point
	 * positions but cannot correct it's bounding box for it
	 */
	protected BoundingBox boundingBox = new BoundingBox();
	
	public BoundingBox boundingBox(double margin, Shape shape)
	{
		return boundingBox.get(pointMatrix(shape),_radius,margin);
	}

	public BoundingBox boundingBox(Shape shape)
	{
		return boundingBox.get(pointMatrix(shape),_radius);
	}
}