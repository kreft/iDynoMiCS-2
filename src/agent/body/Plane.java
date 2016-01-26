package agent.body;

import linearAlgebra.Vector;

/**
 * The constant-normal form of the plane
 * @author baco
 *
 */
public class Plane {

	private double[] normal;
	
	private double d; // d = normal dotproduct point-on-plane
	
	public Plane(double[] normal, double d)
	{
		this.normal = normal;
		this.d = d;
	}
	
	public Plane(double[] normal, double[] point)
	{
		this.normal = normal;
		this.d = Vector.dotProduct(normal, point);
	}
	
	public Plane(double[] pointA, double[] pointB, double[] pointC)
	{
		Vector.normaliseEuclidTo(this.normal, Vector.crossProduct(
				Vector.minus(pointB, pointA), Vector.minus(pointC, pointA)));
		this.d = Vector.dotProduct(normal, pointA);
	}
	
	/**
	 * TODO: testing
	 * @param point
	 * @return
	 */
	public double[] closestPoint(double[] point)
	{
		return Vector.times( Vector.add(point, -distanceToPoint(point)), this.d);
	}
	
	/**
	 * 
	 */
	public double distanceToPoint(double[] point)
	{
		return Vector.dotProduct(this.normal, point) - this.d;
	}
}
