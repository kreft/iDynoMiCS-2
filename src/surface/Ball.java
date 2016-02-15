package surface;

import utility.Copier;
import agent.Body;
import generalInterfaces.HasBoundingBox;

/**
 * 
 * @author baco
 *
 */
public class Ball extends Surface implements HasBoundingBox {
	
	/**
	 * The point of this sphere
	 */
    public Point _point;

    /**
     * the radius of this spehere
     */
    private double _radius;

    public Ball(Point point, double radius)
    {
    	this._point = point;
    	this._radius = radius;
    }


    /**
     * copy constructor
     * @param sphere
     */
	public Ball(Ball sphere) {
		this._point = (Point) Copier.copy(sphere._point);
		this._radius = (double) Copier.copy(sphere._radius);
	}

	public Type type() {
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

}