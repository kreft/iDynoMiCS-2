package agent.body;

import linearAlgebra.Vector;

/**
 * FIXME: Bas - Think of a better name for this class..
 * \brief methods used in collision/attraction detection and response
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
public final class Volume
{	
	/**
	 * Vector that represents the shortest distance between: point-point,
	 * point-line segment and line segment-line segment.
	 */
	static double[] dP;
	
	/**
	 * Represents the closest point on the first line segment expressed as a
	 * fraction of the line segment.
	 */
	static double s;
	
	/**
	 * Represents the closest point on the second line segment expressed as a
	 * fraction of the line segment.
	 */
	static double t;
	
	/**
	 * \brief Updates the net force on two interacting cells as a result from
	 * passive interactions (sphere-sphere).
	 * 
	 * @param a Point of first cell.
	 * @param c Point of second cell.
	 * @param radii Sum of radii.
	 */
	public static void neighbourInteraction(Point a, Point c, Double radii) 
	{
		double[] force = interact(pointPoint(a.getPosition(), c.getPosition()), radii);
		a.addToForce(force);
		c.subtractFromForce(force);
	}
	
	/**
	 * \brief Updates the net force on two interacting cells as a result from
	 * passive interactions (rod-sphere).
	 * 
	 * @param a First point of first cell.
	 * @param b Second point of first cell.
	 * @param c First point of second cell.
	 * @param radii Sum of radii.
	 */
	public static void neighbourInteraction(Point a, Point b, Point c, 
			double radii) 
	{
		double[] force = interact(
			linesegPoint(a.getPosition(), b.getPosition(), c.getPosition()),
			radii);
		a.addToForce(Vector.timesEquals(force, 1.0 - s));
		b.addToForce(Vector.timesEquals(force, s));
		c.subtractFromForce(force);
	}
	
	/**
	 * \brief Updates the net force on two interacting cells as a result from
	 * passive interactions (rod-rod).
	 * 
	 * @param a First point of first cell.
	 * @param b Second point of first cell.
	 * @param c First point of second cell.
	 * @param d Second point of second cell.
	 * @param radii Sum of radii.
	 */
	public static void neighbourInteraction(Point a, Point b, Point c, Point d, 
			Double radii) 
	{
		double[] force = interact(linesegLineseg(a.getPosition(), b.getPosition(), 
				c.getPosition(), d.getPosition()), radii);
		a.addToForce(Vector.timesEquals(force, 1.0 - s));
		b.addToForce(Vector.timesEquals(force, s));
		c.subtractFromForce(Vector.timesEquals(force, 1.0 - t));
		d.subtractFromForce(Vector.timesEquals(force, t));
	}

	/**
	 * \brief Calculate the resulting force (each cell) as a result of a
	 * neighbour interaction.
	 * 
	 * @param distance
	 * 			distance between central units of the two cells (either the
	 * 			center of the sphere or the line-segment of the rod).
	 * @param radii
	 * 			sum of radii
	 * @return returns the total resulting force (each cell) as a result of a
	 * neighbour interaction
	 */
	private static double[] interact(double distance, double radii) 
	{
		double c;
		double p 			= 0.01; 		// pull distance
		double fPull 		= 0.0002;		// pull force scalar
		double fPush 		= 0.6;			// push force scalar
		boolean exponential = true; 		// exponential pull curve
		distance 			-= radii+0.001;	// added margin
		
		// Repulsion
		if (distance < 0.0) 
		{
			c = fPush * distance * distance;
			return Vector.normaliseEuclid(dP, c);
		}
		// Attraction
		else if (distance < p) 
		{
			if (exponential)
			{
				c = fPull * -3.0 * Math.exp(-6.0*distance/p) /
						( 1.0 + Math.exp(6.0 - (36.0*distance) / p) ); 
			}
			else
			{
				c = fPull * - (p-distance) /
						( 1.0 + Math.exp(6.0 - (36.0*distance) / p) );
			}
			return Vector.times(dP, c);
		}
		// Too far away for an interaction.
		return Vector.zeros(dP);
	}
	
	/**
	 * \brief Sphere-sphere distance.
	 * 
	 * @param p Point of first sphere.
	 * @param q Point of second sphere.
	 * @return distance between the two sphere-swept volumes (spheres-sphere).
	 */
	public static double pointPoint(double[] p, double[] q) 
	{
		Vector.setAll(dP, p);
		Vector.subtract(dP, q);
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
	public static double linesegPoint(double[] p0, double[] p1, double[] q0) 
	{
		// ab = p1 - p0
		Vector.subtract(Vector.setAll(dP, p1), p0);
		s  = clamp( Vector.dotProduct(Vector.subtractEquals(q0, p0), dP) 
													/ Vector.normSquare(dP) );
		// dP = (ab*s) + p0 - q0 
		Vector.times(dP, s);
		Vector.add(dP, p0);
		Vector.subtract(dP, q0);
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
	private static double linesegLineseg(double[] p0, double[] p1,
												double[] q0, double[] q1) 
	{
		// r = p0 - q0
		double[] r      = Vector.subtractEquals(p0, q0);
		// d1 = p1 - p0
		double[] d1     = Vector.subtractEquals(p1, p0);
		// d2 = q1 - q0
		double[] d2     = Vector.subtractEquals(q1, q0);
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
		if( t < 0.0 ) 
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
		double[] c1 = Vector.add( p0, Vector.timesEquals(d1,s) );
		// c2 = q0 + (d2*t)
		double[] c2 = Vector.add( q0, Vector.times(Vector.copy(d2),t) );
		// dP = c1 - c2
		Vector.subtract(Vector.setAll(dP, c1), c2);
		return Vector.normEuclid(dP);
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
	private static double clamp(double a) 
	{
		return Math.max( Math.min(a, 1.0), 0.0 );
	}
}
