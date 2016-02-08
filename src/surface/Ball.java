package surface;

import utility.Copier;
import agent.Body;

/**
 * 
 * @author baco
 *
 */
public class Ball extends Surface{
	
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
	
	//TODO
	public BoundingBox boundingBox()
	{
		return null;
	}

}