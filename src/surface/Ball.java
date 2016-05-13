package surface;

import dataIO.ObjectFactory;
import generalInterfaces.Copyable;
import generalInterfaces.HasBoundingBox;
import linearAlgebra.Vector;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Ball extends Surface implements HasBoundingBox, Copyable
{
	/**
	 * Location of the center of this sphere.
	 */
    public Point _point;
    /**
     * Radius of this sphere.
     */
    public double _radius;
    
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
    
    public Ball(Point point, double radius)
    {
    	this._point = point;
    	this._radius = radius;
    }


    /**
     * \brief Copy constructor.
     * 
     * @param sphere
     */
	public Ball(Ball sphere)
	{
		this._point = (Point) ObjectFactory.copy(sphere._point);
		this._radius = (double) ObjectFactory.copy(sphere._radius);
	}

	/**
	 * 
	 * @param center
	 * @param radius
	 */
	public Ball(double[] center, double radius)
	{
    	this._point = new Point(center);
    	this._radius = radius;
	}
	
	/**
	 * \brief Construct a ball with zero radius.
	 * 
	 * @param center
	 */
	public Ball(double[] center)
	{
		this(center, 0.0);
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS & SETTERS
	 ************************************************************************/

	public Type type()
	{
		return Surface.Type.SPHERE;
	}
	
	public double getRadius()
	{
		return _radius;
	}
	
	public void set(double radius, double notUsed)
	{
		this._radius = radius;
	}
	
	public BoundingBox boundingBox(double margin)
	{
		return new BoundingBox(_point.getPosition(),_radius, margin);
	}
	
	public BoundingBox boundingBox()
	{
		return new BoundingBox(_point.getPosition(),_radius);
	}


	@Override
	public Object copy()
	{
		Point p = new Point(Vector.copy(this._point.getPosition()));
		double r = (double) ObjectFactory.copy(this._radius);
		return new Ball(p, r);
	}

}