package surface;

import linearAlgebra.Vector;

/**
 * The constant-normal form of the (infinite) plane
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Plane extends Surface
{
	/**
	 * The normal vector of the plane.
	 */
	public double[] normal;
	
	/**
	 * The dot product of the plane's normal vector with a point on the plane.
	 */
	public double d;

	/**
	 * \brief Construct plane from its normal and the dot product of the
	 * plane's normal vector with a point on the plane.
	 * 
	 * @param normal
	 * @param d
	 */
	public Plane(double[] normal, double d)
	{
		this.normal = normal;
		this.d = d;
	}
	
	/**
	 * \brief Construct plane from its normal vector and a point on the plane.
	 * 
	 * @param normal
	 * @param point
	 */
	public Plane(double[] normal, double[] point)
	{
		this.normal = normal;
		this.d = Vector.dotProduct(normal, point);
	}
	
	/**
	 * \brief Construct plane from 3 points on the plane.
	 * 
	 * @param pointA
	 * @param pointB
	 * @param pointC
	 */
	public Plane(double[] pointA, double[] pointB, double[] pointC)
	{
		this.normal = Vector.normaliseEuclid(Vector.crossProduct(
				Vector.minus(pointB, pointA), Vector.minus(pointC, pointA)));
		this.d = Vector.dotProduct(this.normal, pointA);
	}
	
	/**
	 * Plane.set allows one to move the plane along its normal
	 */
	@Override
	public void set(double d, double notUsed)
	{
		this.d = d;
	}
	
	@Override
	public Type type()
	{
		return Surface.Type.PLANE;
	}
}
