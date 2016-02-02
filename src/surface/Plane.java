package surface;

import linearAlgebra.Vector;

/**
 * The constant-normal form of the plane
 * @author baco
 *
 */
public class Plane extends Surface {

	public double[] normal;
	
	public double d; // d = normal dotproduct point-on-plane

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
	
	public Type type() {
		return Surface.Type.PLANE;
	}
}
