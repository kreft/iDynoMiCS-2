package agent.body;

import utils.Vect;

/**
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
public final class Volume {
	
	/**
	 * Vector that represents the shortest distance between: point-point,
	 * point-line segment and line segment-line segment.
	 */
	static Double[] dP;
	
	/**
	 * Represents the closest point on the first line segment expressed as a
	 * fraction of the line segment
	 */
	static double s;
	
	/**
	 * Represents the closest point on the second line segment expressed as a
	 * fraction of the line segment
	 */
	static double t;
	
	/**
	 * \brief Updates the net force on two interacting cells as a result from
	 * passive interactions (sphere sphere)
	 * @param a
	 * 			point of first cell
	 * @param c
	 * 			point of second cell
	 * @param radii
	 * 			sum of radii
	 */
	public static void neighbourInteraction(Point a, Point c, Double radii) 
	{
		Double[] force = interact(pointPoint(a.getPosition(), c.getPosition()), radii);
		Vect.add(a.getForce(), force);
		Vect.add(c.getForce(),Vect.inverse(force));
	}
	
	/**
	 * \brief Updates the net force on two interacting cells as a result from
	 * passive interactions (sphere sphere)
	 * @param a
	 * 			first point of first cell
	 * @param b
	 * 			second point of first cell
	 * @param c
	 * 			first point of second cell
	 * @param radii
	 * 			sum of radii
	 */
	public static void neighbourInteraction(Point a, Point b, Point c, 
			Double radii) 
	{
		Double[] force = interact(linesegPoint(a.getPosition(), b.getPosition(), 
				c.getPosition()), radii);
		Vect.add(a.getForce(), Vect.product(force,1.0-s));
		Vect.add(b.getForce(), Vect.product(force,s));
		Vect.add(c.getForce(), Vect.inverse(force));
	}
	
	/**
	 * \brief Updates the net force on two interacting cells as a result from
	 * passive interactions (sphere sphere)
	 * @param a
	 * 			first point of first cell
	 * @param b
	 * 			second point of first cell
	 * @param c
	 * 			first point of second cell
	 * @param d
	 * 			second point of second cell
	 * @param radii
	 * 			sum of radii
	 */
	public static void neighbourInteraction(Point a, Point b, Point c, Point d, 
			Double radii) 
	{
		Double[] force = interact(linesegLineseg(a.getPosition(), b.getPosition(), 
				c.getPosition(), d.getPosition()), radii);
		Vect.add(a.getForce(), Vect.product(force,1.0-s));
		Vect.add(b.getForce(), Vect.product(force,s));
		Vect.add(c.getForce(), Vect.product(Vect.inverse(force),t));
		Vect.add(d.getForce(), Vect.product(Vect.inverse(force),1.0-t));
	}

	/**
	 * \brief calculate the resulting force (each cell) as a result of a
	 * neighbour interaction
	 * @param distance
	 * 			distance between central units of the two cells (either the
	 * 			center of the sphere or the line-segment of the rod).
	 * @param radii
	 * 			sum of radii
	 * @return returns the total resulting force (each cell) as a result of a
	 * neighbour interaction
	 */
	private static Double[] interact(Double distance, Double radii) 
	{
		double c;
		double p 			= 0.01; 		// pull distance
		double fPull 		= 0.0002;		// pull force scalar
		double fPush 		= 0.6;			// push force scalar
		boolean exponential = true; 		// exponential pull curve
		distance 			-= radii+0.001;	// added margin
		
		//repulsion
		if (distance < 0.0) 
		{
			c = fPush * distance * distance;
			return Vect.product(Vect.normalize(dP),c);
		} 
		
		//attraction
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
			return Vect.product(dP,c);
		}
		return Vect.zeros(dP.length);
	}
	
	/**
	 * \brief sphere sphere distance.
	 * @param p
	 *			point of first sphere
	 * @param q 
	 * 			point of second sphere
	 * @return distance between the two sphere-swept volumes (spheres sphere)
	 */
	public static Double pointPoint(Double[] p, Double[] q) 
	{
		dP = Vect.minus(p,q);
		return Vect.normE(dP);
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
	public static Double linesegPoint(Double[] p0, Double[] p1, Double[] q0) 
	{
		Double[] ab = Vect.minus(p1, p0);
		s  = clamp(Vect.dot(Vect.minus(q0, p0),ab) / Vect.dot(ab,ab));
		dP = Vect.minus(Vect.sum(p0, Vect.product(ab,s)), q0);
		return Vect.normE(dP);
	}
	
	/**
	 * \brief cylinder cylinder distance.
	 * 
	 * (Ericson 2005, page 148) closest point on two line segments
	 * @param p0
	 * 			First point of first rod
	 * @param p1
	 * 			Second point of first rod
	 * @param q0
	 * 			First point of second rod
	 * @param q1
	 * 			Second point of second rod
	 * @return distance between the two sphere-swept volumes (cylinder cylinder)
	 */
	private static Double linesegLineseg(Double[] p0, Double[] p1, Double[] q0, 
			Double[] q1) 
	{		
		Double[] r  	= Vect.minus(p0, q0);
		Double[] d1 	= Vect.minus(p1, p0);
		Double[] d2 	= Vect.minus(q1, q0);
		double a 		= Vect.dot(d1,d1);
		double e 		= Vect.dot(d2,d2);
		double f 		= Vect.dot(d2,r);
		double c 		= Vect.dot(d1,r);
		double b 		= Vect.dot(d1,d2);
		double denom 	= a*e-b*b;
		
		// s, t = 0.0 if segments are parallel.
		s = ( (denom != 0.0) ? clamp( (b*f-c*e) / denom ) : 0.0 );	
		t = (b*s + f) / e;
		
		if(t<0.0) 
		{
			t = 0.0;
			s = clamp(-c/a);
		} 
		else if (t>1.0) 
		{
			t = 1.0;
			s = clamp((b-c)/a);
		}
		
		Double[] c1 = Vect.sum( p0, Vect.product(d1,s) );
		Double[] c2 = Vect.sum( q0, Vect.product(d2,t) );
		dP = Vect.minus(c1,c2);
		return Vect.normE(dP);
	}

	/**
	 * \brief helper method used in line segment distance algorithms.
	 * @param a: double
	 * @return 0.0 for a < 0.0, 1.0 for a > 1.0 and a for 1.0 > a > 0.0
	 */
	private static double clamp(double a) 
	{
		if(a<0.0)	return 0.0;
		if(a>1.0) 	return 1.0;
		return a;
	}
}
