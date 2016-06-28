package surface;

import generalInterfaces.HasBoundingBox;
import linearAlgebra.Vector;

/**
 * The constant-normal form of the (infinite) plane
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Plane extends Surface implements HasBoundingBox {

	/**
	 * The normal of the plane
	 */
	public double[] normal;
	
	/**
	 * The dot product of the plain's normal with a point on the plane
	 */
	public double d;

	/**
	 * Construct plane from it's normal and the dot product of the plain's 
	 * normal with a point on the plane
	 * @param normal
	 * @param d
	 */
	public Plane(double[] normal, double d)
	{
		this.normal = normal;
		this.d = d;
	}
	
	/**
	 * Construct plane from it's normal and a point on the plane
	 * @param normal
	 * @param point
	 */
	public Plane(double[] normal, double[] point)
	{
		this.normal = normal;
		this.d = Vector.dotProduct(normal, point);
	}
	
	/**
	 * Construct plane from 3 points on the plane
	 * @param pointA
	 * @param pointB
	 * @param pointC
	 */
	public Plane(double[] pointA, double[] pointB, double[] pointC)
	{
		Vector.normaliseEuclidTo(this.normal, Vector.crossProduct(
				Vector.minus(pointB, pointA), Vector.minus(pointC, pointA)));
		this.d = Vector.dotProduct(normal, pointA);
	}
	
	/**
	 * Plane.set allows one to move the plane along its normal
	 */
	public void set(double d, double notUsed)
	{
		this.d = d;
	}
	
	public Type type() {
		return Surface.Type.PLANE;
	}
	
	@Override
	public BoundingBox boundingBox() {

		return this.boundingBox(0.0);
	}

	@Override
	public BoundingBox boundingBox(double margin) {
		double[] lower = new double[normal.length];
		double[] upper = new double[normal.length];
		int n = 0;
		for ( int i = 0; i < normal.length; i++ )
		{
			/* 
			 * a dimension an infinite plane goes into the box will go into 
			 * infinitely as well
			 */
			if (normal[i] == 0.0 )
			{
				lower[i] = -Math.sqrt(Double.MAX_VALUE);
				upper[i] = Math.sqrt(Double.MAX_VALUE);
			}
			/*
			 * if the infinite plane's normal is perpendicular to two dimensions
			 * the bounding box of the infinite plane will cover the entire
			 * domain
			 */
			else if ( n > 0 )
			{
				return new BoundingBox(
						Vector.setAll(lower, -Math.sqrt(Double.MAX_VALUE)),
						Vector.setAll(upper, Math.sqrt(Double.MAX_VALUE)), 
						true);
			}
			/*
			 * if the plane's normal is pointing in a single dimension we can
			 * have a bounding box that is not infinite in that dimension
			 */
			else
			{
				n++;
				lower[i] = normal[i] * d - margin;
				upper[i] = normal[i] * d + margin;
			}
		}
		return new BoundingBox(lower,upper, true);
	}
}
