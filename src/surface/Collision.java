package surface;

import shape.Shape;
import linearAlgebra.Vector;

/**
 * NOTE: this class is not thread-safe.
 * 
 * Methods are based on closest point algorithms from:
 * Ericson, C. (2005). Real-time collision detection. Computer (Vol. 1).
 * 
 * All cells are represented as sphere-swept volumes
 * 
 * On a later stage is class can be expanded to also describe other surfaces
 * with points. this way other objects such as biomass carriers or tubes
 * can be implemented.
 * 
 * @author baco
 */
public class Collision {
	
	public interface CollisionFunction
	{
		public double[] interactionForce(double distance, double[] dP);
	}
	
	public CollisionFunction DefaultCollision = new CollisionFunction()
	{
		public double[] interactionForce(double distance, double[] dP)
		{
			double c;
			double p 			= 0.01; 		// pull distance
			double fPull 		= 0.0002;		// pull force scalar
			double fPush 		= 3.0;			// push force scalar
			boolean exponential = true; 		// exponential pull curve
			
			distance 			-= 0.001;	// added margin
			
			// Repulsion
			if (distance < 0.0) 
			{
				c = Math.abs(fPush * distance); //linear
				Vector.normaliseEuclidEquals(dP, c);
				return dP;
			} 
			// Attraction
			// TODO disabled until it makes sense from  agent tree point of view
//			else if (distance < p) 
//			{
//				if (exponential)
//				{
//					c = fPull * -3.0 * Math.exp(-6.0*distance/p) /
//							( 1.0 + Math.exp(6.0 - (36.0*distance) / p) ); 
//				}
//				else
//				{
//					c = fPull * - (p-distance) /
//							( 1.0 + Math.exp(6.0 - (36.0*distance) / p) );
//				}
//				return Vector.timesEquals(dP, c);
//			}
			// Too far away for an interaction.
			return Vector.zeros(dP);
		}
	};
	
	private CollisionFunction collisionFun;
	
	private Shape computationalDomain;
	
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
	 * flip if the force needs to be added to b and subtracted from a
	 */
	private boolean flip = false;
	
	public Collision(CollisionFunction collisionFunction, Shape compartmentShape)
	{
		if (collisionFunction != null)
			this.collisionFun = collisionFunction;
		this.collisionFun = DefaultCollision;
		this.computationalDomain = compartmentShape;
		this.dP = Vector.zerosDbl(compartmentShape.getNumberOfDimensions());
	}

	/**
	 * apply a collision force on a and b if applicable
	 */
	public void collision(Surface a, Surface b)
	{
		
		//TODO check all the flipin' flipping here
		double[] force = collisionFun.interactionForce(distance(a,b), 
				(flip ? Vector.times(this.dP,-1.0) : this.dP));

		if(flip)
		{
			applyForce(b, force,s);
			applyForce(a, Vector.times(force,-1.0), t);
		} 
		else
		{
			applyForce(a, force,s);
			applyForce(b, Vector.times(force,-1.0), t);
		}
	}
	
	public void applyForce(Surface surf, double[] force, double intersect)
	{
		switch (surf.type())
		{
		case SPHERE:
			((Sphere) surf)._point.addToForce(force);
			break;
		case ROD:
			((Rod) surf)._points[0].addToForce(Vector.times(force,intersect));
			((Rod) surf)._points[1].addToForce(Vector.times(force,1.0-intersect));
			break;
		case PLANE:
			System.out.println("WARNING: Surface Plane does not accept force");
		}
	}
	
	public double distance(Surface a, Surface b)
	{
		Surface first = null;
		Surface second = null;

		// plane interactions
		if (a.type() == Surface.Type.PLANE )
		{
			first = a;
			second = b;
			flip = false;
		} else if (b.type() == Surface.Type.PLANE )
		{
			first = b;
			second = a;
			flip = true;
		}
		if (second == null)
			{}
		else if (second.type() == Surface.Type.SPHERE)
			return planeSphere((Plane) first, (Sphere) second);
		else if (second.type() == Surface.Type.ROD)
			return planeRod((Plane) first, (Rod) second);
		
		// sphere-swept-volume interactions
		if(a.type() == Surface.Type.ROD)
		{
			first = a;
			second = b;
			flip = false;
		} else if(b.type() == Surface.Type.ROD)
		{
			first = b;
			second = a;
			flip = true;
		}
		if (second == null)
			{}
		else if (second.type() == Surface.Type.SPHERE)
			return rodSphere((Rod) first, (Sphere) second);
		else if (second.type() == Surface.Type.ROD)
			return rodRod((Rod) first, (Rod) second);
		
		// sphere sphere
		if(a.type() == Surface.Type.SPHERE)
		{
			first = a;
			second = b;
			flip = false;
			return sphereSphere((Sphere) first, (Sphere) second);
		}
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
	private void setPeriodicDistanceVector(double[] a, double[] b)
	{
		this.dP = computationalDomain.getMinDifference(a,b);
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
		setPeriodicDistanceVector(p, q);
//		Vector.minusTo(dP, p, q);
		return Vector.normEuclid(dP);
	}
	
	public double sphereSphere(Sphere a, Sphere b)
	{
		// a is around b
		if (a.bounding)
			return -pointPoint(a._point.getPosition(),
					b._point.getPosition()) + a.getRadius() - b.getRadius();
		
		// b is around a
		if (b.bounding)
			return -pointPoint(a._point.getPosition(),
					b._point.getPosition()) - a.getRadius() + b.getRadius();
		
		// normal collision
		return pointPoint(a._point.getPosition(),
				b._point.getPosition()) - a.getRadius() - b.getRadius();
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
	
	public double rodSphere(Rod a, Sphere b)
	{
		return linesegPoint(a._points[0].getPosition(),
				a._points[1].getPosition(),
				b._point.getPosition()) - a.getRadius() - b.getRadius();
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

		double[] r      = Vector.minus(p0, q0);
		double[] d1     = Vector.minus(p1, p0);
		double[] d2     = Vector.minus(q1, q0);
		double a 		= Vector.normSquare(d1);
		double e 		= Vector.normSquare(d2);
		double f 		= Vector.dotProduct(d2, r);
		double c 		= Vector.dotProduct(d1, r);
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
	
	public double rodRod(Rod a, Rod b)
	{
		return linesegLineseg(a._points[0].getPosition(),
				a._points[1].getPosition(),
				b._points[0].getPosition(),
				b._points[1].getPosition()) - a.getRadius() - b.getRadius();
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
	
	public double planeSphere(Plane plane, Sphere sphere)
	{
		return planePoint(plane.normal, plane.d, sphere._point.getPosition()) - sphere.getRadius();
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
	
	public double planeRod(Plane plane, Rod rod)
	{
		return planeLineSeg(plane.normal, plane.d, rod._points[0].getPosition(), rod._points[1].getPosition()) - rod.getRadius();
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

