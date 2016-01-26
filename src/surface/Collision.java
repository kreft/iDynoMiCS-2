package surface;

import linearAlgebra.Vector;

public class Collision {
	
	/**
	 * Vector that represents the shortest distance between: point-point,
	 * point-line segment and line segment-line segment.
	 */
	private double[] dP;
	
	/**
	 * Represents the closest point on the first line segment expressed as a
	 * fraction of the line segment.
	 */
	private double s = 0;
	
	/**
	 * Represents the closest point on the second line segment expressed as a
	 * fraction of the line segment.
	 */
	private double t = 0;
	
	/**
	 * Work in progress, stores the vector that points the shortest distance
	 * between a and b
	 */
	private void periodicVectorTo(double[] dP, double[] a, double[] b)
	{
		boolean periodicBoundaries = false;
		if(! periodicBoundaries)
			Vector.minusTo(dP, a, b);
		else
			dP = null; // TODO the shortest distance trough periodic boundaries
	}
	
	/**
	 * 
	 */
	private double[] periodicVector(double[] a, double[] b)
	{
		double[] dP = new double[a.length];
		periodicVectorTo(dP, a, b);
		return dP;
	}
	
	/**
	 * 
	 */
	private void periodicVectorEquals(double[] dP, double[] a)
	{
		periodicVectorTo(dP, dP, a);
	}
	
	/**
	 * \brief Sphere-sphere distance.
	 * 
	 * @param p Point of first sphere.
	 * @param q Point of second sphere.
	 * @return distance between the two sphere-swept volumes (spheres-sphere).
	 */
	private double pointPoint(double[] p, double[] q) 
	{
		periodicVectorTo(dP, p, q);
		return Vector.normEuclid(dP);
	}
	
	/**
	 * \brief sphere cylinder distance.
	 * 
	 * (Ericson 2005, page 127) closest point on line segment to point.
	 * 
	 * @param p0
	 * 			First point of rod
	 * @param p1
	 * 			Second point of rod
	 * @param q0
	 * 			Point of sphere
	 * @return distance between the two sphere-swept volumes (sphere cylinder)
	 */
	private double linesegPoint(double[] p0, double[] p1, double[] q0) 
	{
		// ab = p1 - p0
		Vector.minusTo(dP, p1, p0);
		s  = clamp( Vector.dotProduct( Vector.minus(q0, p0), dP) 
													/ Vector.normSquare(dP) );
		// dP = (ab*s) + p0 - q0 
		Vector.timesEquals(dP, s);
		Vector.addEquals(dP, p0);
		Vector.minusEquals(dP, q0);
		return Vector.normEuclid(dP);
	}
	
	/**
	 * \brief cylinder cylinder distance.
	 * 
	 * (Ericson 2005, page 148) closest point on two line segments
	 * 
	 * @param p0 First point of first rod.
	 * @param p1 Second point of first rod.
	 * @param q0 First point of second rod.
	 * @param q1 Second point of second rod.
	 * @return distance between the two sphere-swept volumes 
	 * (cylinder-cylinder).
	 */
	private double linesegLineseg(double[] p0, double[] p1,
												double[] q0, double[] q1) 
	{		
		// r = p0 - q0
		double[] r      = Vector.minus(p0, q0);
		// d1 = p1 - p0
		double[] d1     = Vector.minus(p1, p0);
		// d2 = q1 - q0
		double[] d2     = Vector.minus(q1, q0);
		// a = d1 . d1 = (p1 - p0)^2
		double a 		= Vector.normSquare(d1);
		// e = d2 . d2 = (q1 - q0)^2
		double e 		= Vector.normSquare(d2);
		// f = d2 . r = (q1 - q0) . (p0 - q0)
		double f 		= Vector.dotProduct(d2, r);
		// c = d1 . r = (p1 - p0) . (p0 - q0)
		double c 		= Vector.dotProduct(d1, r);
		// b = d1 . d2 = (p1 - p0) . (q1 - q0)
		double b 		= Vector.dotProduct(d1, d2);
		// denominator
		double denom 	= (a * e) - (b * b);
		
		// s, t = 0.0 if segments are parallel.
		s = ( (denom != 0.0) ? clamp( (b*f-c*e) / denom ) : 0.0 );	
		t = (b*s + f) / e;
		
		if ( t < 0.0 ) 
		{
			t = 0.0;
			s = clamp(-c/a);
		} 
		else if ( t > 1.0 ) 
		{
			t = 1.0;
			s = clamp((b-c)/a);
		}
		// c1 = p0 + (d1*s)
		double[] c1 = Vector.times(d1, s);
		Vector.addEquals(p0, c1);
		// c2 = q0 + (d2*t)
		double[] c2 = Vector.times(d2, t);
		Vector.addEquals(q0, c1);
		// dP = c1 - c2
		dP = Vector.minus(c1, c2);
		return Vector.normEuclid(dP);
	}
	
	/**
	 * TODO: testing
	 * @param point
	 * @return
	 */
	public double[] closestPointonPlane(double[] point, double[] normal, double d)
	{
		return Vector.times( Vector.add(point, -planePoint(point, normal, d)), d);
	}
	
	/**
	 * 
	 */
	public double planePoint(double[] point, double[] normal, double d)
	{
		return Vector.dotProduct(normal, point) - d;
	}
	
	/**
	 * 
	 */
	public double planeLineSeg(double[] p0, double[] p1, double[] normal, double d)
	{
		double a = planePoint(p0, normal, d);
		double b = planePoint(p1, normal, d);
		if (a < b)
		{
			this.s = 0.0;
			return a;
		}
		if (a > b) 
		{
			this.s = 1.0;
			return b;
		}
		else
		{
			this.s = 0.5;
			return a;
		}
	}

	/**
	 * \brief Helper method used in line segment distance algorithms.
	 * 
	 * <p>Forces <b>a</b> to lie between zero and one (inclusive). If <b>a</b>
	 * is negative, returns zero. If <b>a</b> is greater than one, returns 
	 * one. Otherwise, returns <b>a</b>.</p>
	 * 
	 * @param a A double number.
	 * @return <b>a</b> constrained to the interval [0.0, 1.0].
	 */
	private double clamp(double a) 
	{
		return Math.max( Math.min(a, 1.0), 0.0 );
	}
}

