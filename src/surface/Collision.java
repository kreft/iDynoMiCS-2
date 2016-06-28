package surface;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import shape.Shape;

/**
 * NOTE: this class is not thread-safe.
 * 
 * Distance methods are based on closest point algorithms from:
 * Ericson, C. (2005). Real-time collision detection. Computer (Vol. 1).
 * 
 * All cells are represented as sphere-swept volumes
 * 
 * On a later stage is class can be expanded to also describe other surfaces
 * with points. this way other objects such as biomass carriers or tubes
 * can be implemented.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Collision
{
	/* ***********************************************************************
	 * COLLISION FUNCTIONS
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 */
	// NOTE consider moving into a subpackage
	public interface CollisionFunction
	{
		/**
		 * \brief TODO
		 * 
		 * @return
		 */
		public double forceScalar();
		
		/**
		 * \brief TODO
		 * 
		 * @param distance
		 * @param dP
		 * @return
		 */
		// FIXME Rob [17/5/2016]: clarify what happens to dP: are its values
		// overwritten or preserved?
		public double[] interactionForce(double distance, double[] dP);
	}
	
	public CollisionFunction PullFunction = new CollisionFunction()
	{
		@Override
		public double forceScalar()
		{
			/*
			 * Pull force scalar.
			 */
			// TODO implement as aspect
			return -2.0;
		}
		
		@Override
		public double[] interactionForce(double distance, double[] dP)
		{
			/* Add a small margin. */
			// TODO implement as aspect, a negligible distance may be neglected
			distance -= 0.001;
			/*
			 * If distance is in the range (0, pullRange), apply the pull force.
			 * Otherwise, return a zero vector.
			 */
			if ( distance > 0.0 && distance < pullRange ) 
			{
				/* Linear. */
				double c = Math.abs(this.forceScalar() * distance);
				/* dP is overwritten here. */
				Vector.normaliseEuclidEquals(dP, c);
				return dP;
			} 
			/* dP is not overwritten here. */
			return Vector.zeros(dP);
		}
	};
	
	public CollisionFunction DefaultCollision = new CollisionFunction()
	{
		public double forceScalar()
		{
			/*
			 * Push force scalar.
			 */
			// TODO implement as aspect
			return 6.0;		// push force scalar
		}
		
		public double[] interactionForce(double distance, double[] dP)
		{
			/* Add a small margin. */
			// TODO implement as aspect, a negligible distance may be neglected
			distance += 0.001;
			/*
			 * If distance is negative, apply the repulsive force.
			 * Otherwise, return a zero vector.
			 */
			if ( distance < 0.0 ) 
			{
				/* Linear. */
				double c = Math.abs(this.forceScalar() * distance);
				/* dP is overwritten here. */
				Vector.normaliseEuclidEquals(dP, c);
				return dP;
			}
			/* dP is not overwritten here. */
			return Vector.zeros(dP);
		}
	};

	/* ***********************************************************************
	 * VARIABLES
	 * **********************************************************************/
	
	/**
	 * TODO
	 */
	private CollisionFunction _collisionFun;
	/**
	 * TODO
	 */
	private CollisionFunction _pullFun;
	/**
	 * The shape of the computational domain this collision is happening
	 * inside. Useful for its knowledge of cyclic dimensions and boundary
	 * surfaces.
	 */
	private Shape _computationalDomain;
	
	/**
	 * Vector that represents the shortest distance between: point-point,
	 * point-line segment and line segment-line segment.
	 * 
	 * <p>This vector is set in
	 * {@link #setPeriodicDistanceVector(double[], double[])}.</p>
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
	 * Internal variable used for passing a distance at with surfaces become
	 * attractive.
	 */
	private double pullRange = 0.0;
	
	/**
	 * Flip if the force needs to be applied in the opposite direction to the
	 * default.
	 */
	private boolean flip = false;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param collisionFunction
	 * @param compartmentShape
	 */
	public Collision(CollisionFunction collisionFunction, Shape compartmentShape)
	{
		if ( collisionFunction == null )
			this._collisionFun = DefaultCollision;
		else
			this._collisionFun = collisionFunction;
		this._computationalDomain = compartmentShape;
		this.dP = Vector.zerosDbl(compartmentShape.getNumberOfDimensions());
		
		//FIXME testing purposes
		this._pullFun = PullFunction;
	}

	/**
	 * \brief TODO
	 * 
	 * @param aShape
	 */
	public Collision(Shape aShape)
	{
		this(null, aShape);
	}
	
	/* ***********************************************************************
	 * FORCE METHODS
	 * **********************************************************************/
	
	/**
	 * @return The greatest possible magnitude of a force in this system.
	 */
	public double getMaxForceScalar()
	{
		return Math.max(
				Math.abs(this._collisionFun.forceScalar()), 
				Math.abs(this._pullFun.forceScalar()));
	}
	
	/**
	 * \brief Apply a collision force on two surfaces, if applicable.
	 * 
	 * @param a One surface object.
	 * @param b Another surface object.
	 * @param pullDistance The maximum distance between surfaces before they
	 * become attractive.
	 */
	public void collision(Surface a, Surface b, double pullDistance)
	{
		this.pullRange = pullDistance;
		double dist = this.distance(a, b);
		/* 
		 * If the two surfaces overlap, then they should push each other away.
		 */
		if ( dist < 0.0 )
		{
			double[] force = this._collisionFun.interactionForce(dist, 
					(this.flip ? Vector.reverse(this.dP) : this.dP));
	
			if( this.flip )
			{
				this.applyForce(b, force, this.s);
				this.applyForce(a, Vector.reverse(force), this.t);
			} 
			else
			{
				this.applyForce(a, force, this.s);
				this.applyForce(b, Vector.reverse(force), this.t);
			}
		}
		/*
		 * If pull distance is greater than zero, then there may be attraction
		 * between the two surfaces.
		 */
		else if ( pullDistance > 0.0 )
		{
			double[] force = this._pullFun.interactionForce(dist, 
					(this.flip ? Vector.reverse(this.dP) : this.dP));

			if( this.flip )
			{
				this.applyForce(a, force, this.s);
				this.applyForce(b, Vector.reverse(force), this.t);
			} 
			else
			{
				this.applyForce(b, force, this.s);
				this.applyForce(a, Vector.reverse(force), this.t);
			}
		}
		/* Reset pull distance: this is very important! */
		this.pullRange = 0.0;
	}

	/**
	 * \brief TODO
	 * 
	 * @param allA
	 * @param allB
	 * @param pullDistance
	 */
	public void collision(
			Collection<Surface> allA, Collection<Surface> allB, double pullDistance)
	{
		for ( Surface a : allA )
			for ( Surface b : allB )
				this.collision(a, b, pullDistance);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param surf
	 * @param force
	 * @param intersect
	 */
	private void applyForce(Surface surf, double[] force, double intersect)
	{
		switch ( surf.type() )
		{
		case SPHERE:
			((Ball) surf)._point.addToForce(force);
			break;
		case ROD:
			((Rod) surf)._points[0].addToForce(Vector.times(force,1.0-intersect));
			((Rod) surf)._points[1].addToForce(Vector.times(force,intersect));
			break;
		case PLANE:
			Log.out(Tier.BULK,"WARNING: Surface Plane does not accept force");
		}
	}
	
	/* ***********************************************************************
	 * KEY DISTANCE METHODS
	 * **********************************************************************/

	/**
	 * colliding
	 * @param a
	 * @param b
	 * @param margin
	 * @return
	 */
	public boolean colliding(Surface a, Surface b, double margin)
	{
		return ( distance( a, b ) < margin );
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param margin
	 * @return
	 */
	public double distance(Surface a, Surface b, double margin)
	{
		return distance( a, b ) - margin;
	}
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double distance(Surface a, Surface b)
	{
		/*
		 * First check that both Surfaces exist.
		 */
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null surface given");
		/* Plane interactions. */
		if ( a.type() == Surface.Type.PLANE )
		{
			flip = false;
			return this.assessPlane((Plane) a, b);
		}
		else if ( b.type() == Surface.Type.PLANE )
		{
			flip = true;
			return this.assessPlane((Plane) b, a);
		}
		/* Sphere-swept-volume interactions. */
		if ( a.type() == Surface.Type.ROD )
		{
			this.flip = false;
			return this.assessRod((Rod) a, b); 
		}
		else if ( b.type() == Surface.Type.ROD )
		{
			this.flip = true;
			return this.assessRod((Rod) b, a);
		}
		/* Sphere-sphere interactions. */
		if( a.type() == Surface.Type.SPHERE )
		{
			this.flip = false;
			return this.sphereSphere((Ball) a, (Ball) b);
		}
		else
		{
			System.out.println("WARNING: undefined Surface type");
			return 0.0;
		}
		
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param p
	 * @return
	 */
	public double distance(Surface a, double[] p)
	{
		switch ( a.type() )
		{
		case SPHERE :
			return this.spherePoint((Ball) a, p);
		case ROD :
			return this.rodPoint((Rod) a, p);
		case PLANE:
			return planePoint((Plane) a, p);
		}
		return 0.0;
	}
	
	/* ***********************************************************************
	 * PRIVATE ASSESMENT METHODS
	 * **********************************************************************/
	
	/**
	 * \brief Calculate the distance between a Plane and another surface of
	 * unknown type.
	 * 
	 * @param plane
	 * @param otherSurface
	 * @return
	 */
	private double assessPlane(Plane plane, Surface otherSurface)
	{
		// TODO plane-plane 
		if ( otherSurface.type() == Surface.Type.SPHERE )
			return planeSphere(plane, (Ball) otherSurface);
		if ( otherSurface.type() == Surface.Type.ROD )
			return this.planeRod(plane, (Rod) otherSurface);
		// TODO safety
		return 0.0;
	}
	
	/**
	 * \brief Calculate the distance between a Rod and another surface of
	 * unknown type.
	 * 
	 * @param rod
	 * @param otherSurface
	 * @return
	 */
	private double assessRod(Rod rod, Surface otherSurface)
	{
		if ( otherSurface.type() == Surface.Type.SPHERE )
			return this.rodSphere(rod, (Ball) otherSurface);
		if ( otherSurface.type() == Surface.Type.ROD )
			return this.rodRod(rod, (Rod) otherSurface);
		// TODO safety
		return 0.0;
	}
	/*************************************************************************
	 * PRIVATE DISTANCE METHODS
	 ************************************************************************/

	/**
	 * \brief Stores the vector that points the shortest distance between two
	 * locations.
	 * 
	 * @param a
	 * @param b
	 */
	// NOTE Work in progress
	private void setPeriodicDistanceVector(double[] a, double[] b)
	{
		this._computationalDomain.getMinDifferenceTo(this.dP, a, b);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double[] minDistance(double[] a, double[] b)
	{
		return this._computationalDomain.getMinDifference(a,b);
	}
	
	/**
	 * \brief Point-point distance.
	 * 
	 * @param p First point.
	 * @param q Second point.
	 * @return Distance between the two points.
	 */
	private double pointPoint(double[] p, double[] q) 
	{
		this.setPeriodicDistanceVector(p, q);
//		Vector.minusTo(dP, p, q);
		return Vector.normEuclid(dP);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param p
	 * @return
	 */
	private double spherePoint(Ball a, double[] p)
	{
		/*
		 * First find the distance between the point and the centre of the
		 * sphere. 
		 */
		double out = pointPoint(a.getCenter(), p);
		/*
		 * Subtract the sphere's radius to find the distance between the point
		 * and the surface of the sphere.
		 */
		return out - a.getRadius();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double sphereSphere(Ball a, Ball b)
	{
		double pointPoint = pointPoint(a.getCenter(), b.getCenter());
		/* a is around b. */
		if ( a.bounding )
			return - pointPoint + a.getRadius() - b.getRadius();
		/* b is around a. */
		if ( b.bounding )
			return - pointPoint - a.getRadius() + b.getRadius();
		/* Normal collision. */
		return pointPoint - a.getRadius() - b.getRadius();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param normal
	 * @param d
	 * @param p0
	 * @param p1
	 * @return
	 */
	private double planeLineSeg(double[] normal, double d, double[] p0, double[] p1)
	{
		dP = Vector.reverse(normal);
		double a = planePoint(normal, d, p0);
		double b = planePoint(normal, d, p1);
		if ( a < b )
		{
			this.s = 0.0;
			return a;
		}
		if ( a > b ) 
		{
			this.s = 1.0;
			return b;
		}
		/* a = b */
		this.s = 0.5;
		return a;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param plane
	 * @param rod
	 * @return
	 */
	private double planeRod(Plane plane, Rod rod)
	{
		/*
		 * First find the distance between the plane and the axis of the rod. 
		 */
		double out = planeLineSeg(plane.normal, plane.d, 
				rod._points[0].getPosition(), rod._points[1].getPosition());
		/*
		 * Subtract the rod's radius to find the distance between the plane and
		 * the rod's surface.
		 */
		return out - rod.getRadius();
	}
	
	/**
	 * \brief Calculates the distance between a line segment and a point.
	 * 
	 * <p>(Ericson 2005, page 127) closest point on line segment to point.</p>
	 * 
	 * @param p0 First point of rod
	 * @param p1 Second point of rod
	 * @param q0 Point of sphere
	 * @return distance between the line segment and the point.
	 */
	public double linesegPoint(double[] p0, double[] p1, double[] q0) 
	{
		// ab = p1 - p0
		this.setPeriodicDistanceVector(p1, p0);
		s  = clamp( Vector.dotProduct( Vector.minus(q0, p0), dP) 
													/ Vector.normSquare(dP) );
		// dP = (ab*s) + p0 - q0 
		Vector.timesEquals(dP, s);
		Vector.addEquals(dP, p0);
		Vector.minusEquals(dP, q0);
		return Vector.normEuclid(dP);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param p
	 * @return
	 */
	public double rodPoint(Rod a, double[] p)
	{
		/*
		 * First find the distance between the axis of the rod and the point. 
		 */
		double out = linesegPoint(a._points[0].getPosition(), 
				a._points[1].getPosition(), p);
		/*
		 * Subtract the rod's radius to find the distance between the point and
		 * the rod's surface.
		 */
		return out - a.getRadius();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aRod
	 * @param aBall
	 * @return
	 */
	public double rodSphere(Rod aRod, Ball aBall)
	{
		/*
		 * First find the distance between the axis of the rod and the centre
		 * of the sphere. 
		 */
		double out = linesegPoint(aRod._points[0].getPosition(),
				aRod._points[1].getPosition(),
				aBall.getCenter());
		/*
		 * Subtract the radii of both to find the distance between their
		 * surfaces.
		 */
		return out - aRod.getRadius() - aBall.getRadius();
	}
	
	/**
	 * \brief Distance between two line segments.
	 * 
	 * <p>(Ericson 2005, page 148) closest point on two line segments.</p>
	 * 
	 * @param p0 First point of first rod.
	 * @param p1 Second point of first rod.
	 * @param q0 First point of second rod.
	 * @param q1 Second point of second rod.
	 * @return distance between the two line segments.
	 */
	public double linesegLineseg(double[] p0, double[] p1,
												double[] q0, double[] q1) 
	{		

		double[] r      = minDistance(p0, q0);
		double[] d1     = minDistance(p1, p0);
		double[] d2     = minDistance(q1, q0);
		double a 		= Vector.normSquare(d1);
		double e 		= Vector.normSquare(d2);
		double f 		= Vector.dotProduct(d2, r);
		double c 		= Vector.dotProduct(d1, r);
		double b 		= Vector.dotProduct(d1, d2);
		double denominator 	= (a * e) - (b * b);
		
		/* s, t = 0.0 if segments are parallel. */
		if ( denominator == 0.0 )
			this.s = 0.0;
		else
			this.s = clamp( (b*f-c*e) / denominator );	
		this.t = (b*this.s + f) / e;
		/*
		 * TODO explain
		 */
		if ( this.t < 0.0 ) 
		{
			this.t = 0.0;
			this.s = clamp(-c/a);
		} 
		else if ( this.t > 1.0 ) 
		{
			this.t = 1.0;
			this.s = clamp((b-c)/a);
		}
		
		/* c1 = p0 + (d1*s) */
		double[] c1 = Vector.times(d1, this.s);
		Vector.addEquals(c1, p0);
		
		/* c2 = q0 + (d2*t) */
		double[] c2 = Vector.times(d2, this.t);
		Vector.addEquals(c2, q0);

		/* dP = c1 - c2 */
		this.setPeriodicDistanceVector(c1, c2);
		return Vector.normEuclid(this.dP);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double rodRod(Rod a, Rod b)
	{
		/*
		 * First find the distance between the axes of the two rods. 
		 */
		double out = linesegLineseg(a._points[0].getPosition(),
				a._points[1].getPosition(),
				b._points[0].getPosition(),
				b._points[1].getPosition());
		/*
		 * Subtract the radii of both rods to find the distance between their
		 * surfaces.
		 */
		return out - a.getRadius() - b.getRadius();
	}
	
	/**
	 * \brief Distance between a plane and a point.
	 * 
	 * @param plane
	 * @param point
	 * @return
	 */
	public double planePoint(Plane plane, double[] point)
	{
		this.dP = Vector.reverse(plane.normal);
		return Vector.dotProduct(plane.normal, point) - plane.d;
	}
	
	/**
	 * \brief TODO: testing
	 * 
	 * @param normal
	 * @param d
	 * @param point
	 * @return
	 */
	public double[] closestPointOnPlane(double[] normal, double d, double[] point)
	{
		/*
		 * TODO explain
		 */
		double[] out = Vector.add(point, -planePoint(normal, d, point));
		Vector.timesEquals(out, d);
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param plane
	 * @param sphere
	 * @return
	 */
	public double planeSphere(Plane plane, Ball sphere)
	{
		/*
		 * First find the distance between the plane and the centre of the
		 * sphere. 
		 */
		double out = planePoint(plane.normal, plane.d, sphere.getCenter());
		/*
		 * Subtract the rod's radius to find the distance between the plane and
		 * the rod's surface.
		 */
		return out - sphere.getRadius();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param normal
	 * @param d
	 * @param point
	 * @return
	 */
	public double planePoint(double[] normal, double d, double[] point)
	{
		this.dP = Vector.reverse(normal);
		return Vector.dotProduct(normal, point) - d;
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
	public static double clamp(double a) 
	{
		return Math.max( Math.min(a, 1.0), 0.0 );
	}
}

