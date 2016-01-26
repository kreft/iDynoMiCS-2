package surface;

import agent.body.Point;
import linearAlgebra.Vector;

/**
 * 
 * Methods are based on closest point algorithms from:
 * Ericson, C. (2005). Real-time collision detection. Computer (Vol. 1).
 * 
 * All cells are represented as sphere-swept volumes
 * 
 * On a later stage is class can be expanded to also describe other surfaces
 * with points. this way other objects such as biomass carriers or tubes
 * can be implemented.
 */
public class Collision {
	
	public interface CollisionFunction
	{
		public double[] interactionForce(double distance);
	}
	
	private CollisionFunction collisionFun;
	
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

	public void collision(Surface a, Surface b)
	{
		collisionFun.interactionForce(distance(a,b));
	}
	
	public double distance(Surface a, Surface b)
	{
		Surface first = null;
		Surface second = null;
		
		// filaments to be implemented
		if ((a.type() == Surface.Type.STRAND ) || (b.type() == Surface.Type.STRAND ))
		{
			System.out.println("WARNING: STRAND shape not supported (yet) collision detect..");
			return 0.0;
		}
		
		// plane interactions
		if (a.type() == Surface.Type.PLANE )
		{
			first = a;
			second = b;
		} else if (b.type() == Surface.Type.PLANE )
		{
			first = b;
			second = a;
		}
		if (second.type() == Surface.Type.SPHERE)
			return planeSphere((Plane) first, (SphereSweptVolume) second);
		else if (second.type() == Surface.Type.ROD)
			return planeRod((Plane) first, (SphereSweptVolume) second);
		
		// sphere-swept-volume interactions
		if(a.type() == Surface.Type.ROD)
		{
			first = a;
			second = b;
		} else if(b.type() == Surface.Type.ROD)
		{
			first = b;
			second = a;
		}
		if (second.type() == Surface.Type.SPHERE)
			return rodSphere((SphereSweptVolume) first, (SphereSweptVolume) second);
		else if (second.type() == Surface.Type.ROD)
			return rodRod((SphereSweptVolume) first, (SphereSweptVolume) second);
		else if (second.type() != null)
			return sphereSphere((SphereSweptVolume) first, (SphereSweptVolume) second);
		else
		{
			System.out.println("WARNING: undefined Surface type");
			return 0.0;
		}
		
	}
	
	
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
	public double pointPoint(double[] p, double[] q) 
	{
		periodicVectorTo(dP, p, q);
		return Vector.normEuclid(dP);
	}
	
	public double sphereSphere(SphereSweptVolume a, SphereSweptVolume b)
	{
		return pointPoint(a._points.get(0).getPosition(),
				b._points.get(0).getPosition()) - a._radius - b._radius;
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
	public double linesegPoint(double[] p0, double[] p1, double[] q0) 
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
	
	public double rodSphere(SphereSweptVolume a, SphereSweptVolume b)
	{
		return linesegPoint(a._points.get(0).getPosition(),
				a._points.get(1).getPosition(),
				b._points.get(0).getPosition()) - a._radius - b._radius;
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
	public double linesegLineseg(double[] p0, double[] p1,
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
	
	public double rodRod(SphereSweptVolume a, SphereSweptVolume b)
	{
		return linesegLineseg(a._points.get(0).getPosition(),
				a._points.get(1).getPosition(),
				b._points.get(0).getPosition(),
				b._points.get(1).getPosition()) - a._radius - b._radius;
	}
	
	/**
	 * TODO: testing
	 * @param point
	 * @return
	 */
	public double[] closestPointonPlane(double[] normal, double d, double[] point)
	{
		return Vector.times( Vector.add(point, -planePoint(normal, d, point)), d);
	}
	
	/**
	 * 
	 */
	public double planePoint(double[] normal, double d, double[] point)
	{
		return Vector.dotProduct(normal, point) - d;
	}
	
	public double planeSphere(Plane plane, SphereSweptVolume sphere)
	{
		return planePoint(plane.normal, plane.d, sphere._points.get(0).getPosition()) - sphere._radius;
	}
	
	/**
	 * 
	 */
	public double planeLineSeg(double[] normal, double d, double[] p0, double[] p1)
	{
		double a = planePoint(normal, d, p0);
		double b = planePoint(normal, d, p1);
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
	
	public double planeRod(Plane plane, SphereSweptVolume rod)
	{
		return planeLineSeg(plane.normal, plane.d, rod._points.get(0).getPosition(), rod._points.get(1).getPosition()) - rod._radius;
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

