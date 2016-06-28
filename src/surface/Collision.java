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
			if ( distance > 0.0 && distance < _pullRange ) 
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
	 * The function used to evaluate repulsive forces between surfaces that
	 * overlap.
	 */
	private CollisionFunction _collisionFun;
	/**
	 * The function used to evaluate attractive forces between surfaces that
	 * are close.
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
	 * {@link #setPeriodicDistanceVector(double[], double[])}. It is
	 * overwritten in
	 * CollisionFunction.interactionForce(double distance, double[] dP), but
	 * only if flip is false and there is a force to apply.</p>
	 */
	private double[] dP;
	
	/**
	 * Represents the closest point on the first line segment expressed as a
	 * fraction of the line segment.
	 */
	private double s = 0.0;
	
	/**
	 * Represents the closest point on the second line segment expressed as a
	 * fraction of the line segment.
	 */
	private double t = 0.0;
	
	/**
	 * Internal variable used for passing a distance at with surfaces become
	 * attractive.
	 * 
	 * <p>This is set in {@link #collision(Collection, Collection, double)}
	 * only.</p>
	 */
	private double _pullRange = 0.0;
	
	/**
	 * Flip if the force needs to be applied in the opposite direction to the
	 * default.
	 * 
	 * <p>This is set in {@link #distance(Surface, Surface)} and used in
	 * {@link #collision(Collection, Collection, double)}.</p>
	 */
	private boolean _flip = false;
	
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
			this._collisionFun = this.DefaultCollision;
		else
			this._collisionFun = collisionFunction;
		this._computationalDomain = compartmentShape;
		this.dP = Vector.zerosDbl(compartmentShape.getNumberOfDimensions());
		
		//FIXME testing purposes
		this._pullFun = this.PullFunction;
	}

	/**
	 * \brief Construct a collision 
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
	 * <p>This method always also sets the internal variables {@link #_flip}
	 * and {@link #dP}. It may also set {@link #s} and {@link #t}, depending on
	 * the surface types.</p>
	 * 
	 * @param a One surface object.
	 * @param b Another surface object.
	 * @param pullDistance The maximum distance between surfaces before they
	 * become attractive.
	 */
	public void collision(Surface a, Surface b, double pullDistance)
	{
		this._pullRange = pullDistance;
		double dist = this.distance(a, b);
		/* 
		 * If the two surfaces overlap, then they should push each other away.
		 */
		if ( dist < 0.0 )
		{
			double[] force = this._collisionFun.interactionForce(dist, 
					(this._flip ? Vector.reverse(this.dP) : this.dP));
	
			if( this._flip )
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
					(this._flip ? Vector.reverse(this.dP) : this.dP));

			if( this._flip )
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
		this._pullRange = 0.0;
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
	 * \brief Apply a force vector to a surface.
	 * 
	 * @param surf Surface object.
	 * @param force Force vector: both direction and magnitude are important.
	 * @param intersect Force modifier for more complex surfaces.
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
	 * \brief Check if the distance between two surfaces less than a given
	 * margin.
	 * 
	 * @param a One surface, of unknown type.
	 * @param b Another surface, of unknown type.
	 * @param margin Minimum distance between the two surfaces.
	 * @return True if the distance between the two surfaces is less than
	 * the margin given, otherwise false.
	 */
	public boolean areColliding(Surface a, Surface b, double margin)
	{
		return ( this.distance( a, b, margin ) < 0.0 );
	}
	
	/**
	 * \brief Calculate the distance between two surfaces, subtracting a given
	 * margin.
	 * 
	 * <p>This method always also sets the internal variables {@link #_flip}
	 * and {@link #dP}. It may also set {@link #s} and {@link #t}, depending on
	 * the surface types.</p>
	 * 
	 * @param a One surface, of unknown type.
	 * @param b Another surface, of unknown type.
	 * @param margin Distance to exclude.
	 * @return The minimum distance between the two surfaces.
	 */
	public double distance(Surface a, Surface b, double margin)
	{
		return this.distance( a, b ) - margin;
	}
	
	/**
	 * \brief Calculate the distance between two surfaces.
	 * 
	 * <p>This method always also sets the internal variables {@link #_flip}
	 * and {@link #dP}. It may also set {@link #s} and {@link #t}, depending on
	 * the surface types.</p>
	 * 
	 * @param a One surface, of unknown type.
	 * @param b Another surface, of unknown type.
	 * @return The minimum distance between the two surfaces.
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
			this._flip = false;
			return this.assessPlane((Plane) a, b);
		}
		else if ( b.type() == Surface.Type.PLANE )
		{
			this._flip = true;
			return this.assessPlane((Plane) b, a);
		}
		/* Sphere-swept-volume interactions. */
		if ( a.type() == Surface.Type.ROD )
		{
			this._flip = false;
			return this.assessRod((Rod) a, b); 
		}
		else if ( b.type() == Surface.Type.ROD )
		{
			this._flip = true;
			return this.assessRod((Rod) b, a);
		}
		/* Sphere-sphere interactions. */
		if( a.type() == Surface.Type.SPHERE )
		{
			this._flip = false;
			return this.sphereSphere((Ball) a, (Ball) b);
		}
		else
		{
			System.out.println("WARNING: undefined Surface type");
			return 0.0;
		}
		
	}
	
	/**
	 * \brief Calculate the distance from a surface to a point.
	 * 
	 * <p>This method always also sets the internal variable {@link #dP}.
	 * It may also set {@link #s}, depending on the surface type.</p>
	 * 
	 * @param a Surface object, of unknown type.
	 * @param p A point in space.
	 * @return The minimum distance from the surface to the point.
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
			return this.planePoint((Plane) a, p);
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
	 * <p>This method always also sets the internal variable {@link #dP}.
	 * It may also set {@link #s}, depending on the other surface type.</p>
	 * 
	 * @param plane Surface of an infinite plane.
	 * @param otherSurface Another surface object, of unknown type.
	 * @return The minimum distance between the two surfaces.
	 */
	private double assessPlane(Plane plane, Surface otherSurface)
	{
		// TODO plane-plane 
		if ( otherSurface.type() == Surface.Type.SPHERE )
			return this.planeSphere(plane, (Ball) otherSurface);
		if ( otherSurface.type() == Surface.Type.ROD )
			return this.planeRod(plane, (Rod) otherSurface);
		// TODO safety
		return 0.0;
	}
	
	/**
	 * \brief Calculate the distance between a Rod and another surface of
	 * unknown type.
	 * 
	 * <p>This method always also sets the internal variables {@link #s} and 
	 * {@link #dP}. It may also set {@link #t}, depending on the other surface
	 * type.</p>
	 * 
	 * @param rod Rod surface.
	 * @param otherSurface Another surface object, of unknown type.
	 * @return The minimum distance between the two surfaces.
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
	 * <p>Neither vector is changed by this method.</p>
	 * 
	 * @param a One point in space.
	 * @param b Another point in space.
	 */
	// NOTE Work in progress
	private void setPeriodicDistanceVector(double[] a, double[] b)
	{
		this._computationalDomain.getMinDifferenceTo(this.dP, a, b);
	}
	
	/**
	 * \brief Calculate the minimum distance between two points in space.
	 * 
	 * <p>Neither vector is changed by this method.</p>
	 * 
	 * @param a One point in space.
	 * @param b Another point in space.
	 * @return The minmum distance between them.
	 */
	private double[] minDistance(double[] a, double[] b)
	{
		return this._computationalDomain.getMinDifference(a,b);
	}
	
	/**
	 * \brief Point-point distance.
	 * 
	 * <p>This method also sets the internal variable {@link #dP}.
	 * It returns the Euclidean norm of {@link #dP}.</p>
	 * 
	 * @param p First point.
	 * @param q Second point.
	 * @return Distance between the two points.
	 */
	private double pointPoint(double[] p, double[] q) 
	{
		this.setPeriodicDistanceVector(p, q);
		return Vector.normEuclid(dP);
	}
	
	/**
	 * \brief Calculate the distance from the surface of a sphere to a point.
	 * 
	 * <p>This method also sets the internal variable {@link #dP}.</p>
	 * 
	 * @param a A ball object.
	 * @param p A point in space.
	 * @return The minimum distance from the surface of the sphere to the point.
	 */
	private double spherePoint(Ball a, double[] p)
	{
		/*
		 * First find the distance between the point and the centre of the
		 * sphere. 
		 */
		double out = this.pointPoint(a.getCenter(), p);
		/*
		 * Subtract the sphere's radius to find the distance between the point
		 * and the surface of the sphere.
		 */
		return out - a.getRadius();
	}
	
	/**
	 * \brief Calculate the distance between two spheres.
	 * 
	 * <p>This method also sets the internal variable {@link #dP}.</p>
	 * 
	 * @param a A ball object representing one sphere.
	 * @param b A ball object representing another sphere.
	 * @return The minimum distance between the surfaces of the two spheres.
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
	 * \brief Calculate the distance between an infinite plane and the segment
	 * of a line.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #dP}.</p>
	 * 
	 * @param normal Normal vector of the plane.
	 * @param d 
	 * @param p0 Point in space at one end of the line segment.
	 * @param p1 Point in space at the other end of the line segment.
	 * @return The minimum distance between the plane and the line segment.
	 */
	private double planeLineSeg(
			double[] normal, double d, double[] p0, double[] p1)
	{
		Vector.reverseTo(this.dP, normal);
		double a = this.planePoint(normal, d, p0);
		double b = this.planePoint(normal, d, p1);
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
	 * \brief Calculate the distance between an infinite plane and the surface 
	 * of a rod.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #dP}.</p>
	 * 
	 * @param plane Infinite plane.
	 * @param rod Rod surface.
	 * @return Minimum distance between the plane and the rod.
	 */
	private double planeRod(Plane plane, Rod rod)
	{
		/*
		 * First find the distance between the plane and the axis of the rod. 
		 */
		double out = this.planeLineSeg(
				plane.normal, plane.d, 
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
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #dP}. It returns the Euclidean norm of {@link #dP}.</p>
	 * 
	 * <p>(Ericson 2005, page 127) closest point on line segment to point.</p>
	 * 
	 * @param p0 First point of rod.
	 * @param p1 Second point of rod.
	 * @param q0 Point of sphere.
	 * @return Minimum distance between the line segment and the point.
	 */
	public double linesegPoint(double[] p0, double[] p1, double[] q0) 
	{
		/* ab = p1 - p0 */
		this.setPeriodicDistanceVector(p1, p0);
		this.s = Vector.dotProduct( Vector.minus(q0, p0), this.dP);
		this.s /= Vector.normSquare(this.dP);
		this.s  = clamp( this.s );
		/* dP = (ab*s) + p0 - q0 */
		Vector.timesEquals(this.dP, this.s);
		Vector.addEquals(this.dP, p0);
		Vector.minusEquals(this.dP, q0);
		return Vector.normEuclid(this.dP);
	}
	
	/**
	 * \brief Calculate the distance between the surface of a rod and a point
	 * in space.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #dP}.</p>
	 * 
	 * @param aRod A rod surface.
	 * @param p A point in space.
	 * @return The minimum distance between the surface of the rod and the
	 * point.
	 */
	public double rodPoint(Rod aRod, double[] p)
	{
		/*
		 * First find the distance between the axis of the rod and the point. 
		 */
		double out = this.linesegPoint(
				aRod._points[0].getPosition(), 
				aRod._points[1].getPosition(), p);
		/*
		 * Subtract the rod's radius to find the distance between the point and
		 * the rod's surface.
		 */
		return out - aRod.getRadius();
	}
	
	/**
	 * \brief Calculate the distance between the surfaces of a rod and of a
	 * sphere.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #dP}.</p>
	 * 
	 * @param aRod A rod surface.
	 * @param aBall A sphere surface.
	 * @return The minimum distance between the surface of the rod and the
	 * surface of the sphere.
	 */
	public double rodSphere(Rod aRod, Ball aBall)
	{
		/*
		 * First find the distance between the axis of the rod and the centre
		 * of the sphere. 
		 */
		double out = this.linesegPoint(
				aRod._points[0].getPosition(),
				aRod._points[1].getPosition(),
				aBall.getCenter());
		/*
		 * Subtract the radii of both to find the distance between their
		 * surfaces.
		 */
		return out - aRod.getRadius() - aBall.getRadius();
	}
	
	/**
	 * \brief Calculate the distance between two line segments.
	 * 
	 * <p>This method also sets the internal variables {@link #s}, {@link #t}, 
	 * and {@link #dP}. It returns the Euclidean norm of {@link #dP}.</p>
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

		double[] r	= this.minDistance(p0, q0);
		double[] d1	= this.minDistance(p1, p0);
		double[] d2	= this.minDistance(q1, q0);
		double a 	= Vector.normSquare(d1);
		double e 	= Vector.normSquare(d2);
		double f 	= Vector.dotProduct(d2, r);
		double c 	= Vector.dotProduct(d1, r);
		double b 	= Vector.dotProduct(d1, d2);
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
		/*
		 * Note that below we overwrite the d1 and d2 vectors with the values
		 * for c1 and c2 (for efficiency).
		 */
		/* c1 = p0 + (d1*s) */
		Vector.timesEquals(d1, this.s);
		Vector.addEquals(d1, p0);
		/* c2 = q0 + (d2*t) */
		Vector.timesEquals(d2, this.t);
		Vector.addEquals(d2, q0);
		/* dP = c1 - c2 */
		this.setPeriodicDistanceVector(d1, d2);
		return Vector.normEuclid(this.dP);
	}
	
	/**
	 * \brief Calculate the minimum distance between the surfaces of two rods.
	 * 
	 * <p>This method also sets the internal variables {@link #s}, {@link #t}, 
	 * and {@link #dP}.</p>
	 * 
	 * @param a One rod.
	 * @param b Another rod.
	 * @return The minimum distance between the surfaces of the two rods.
	 */
	private double rodRod(Rod a, Rod b)
	{
		/*
		 * First find the distance between the axes of the two rods. 
		 */
		double out = this.linesegLineseg(
				a._points[0].getPosition(),
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
	 * <p>This method also sets the internal variable {@link #dP}.</p>
	 * 
	 * @param plane An infinite plane.
	 * @param point A point in space.
	 * @return The minimum distance between the plane and the point.
	 */
	public double planePoint(Plane plane, double[] point)
	{
		Vector.reverseTo(this.dP, plane.normal);
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
	public double[] closestPointOnPlane(
			double[] normal, double d, double[] point)
	{
		/*
		 * TODO explain
		 */
		double[] out = Vector.add(point, -this.planePoint(normal, d, point));
		Vector.timesEquals(out, d);
		return out;
	}
	
	/**
	 * \brief Calculate the distance between an infinite plane and the surface
	 * of a sphere.
	 * 
	 * <p>This method also sets the internal variable {@link #dP}.</p>
	 * 
	 * @param plane An infinite plane.
	 * @param sphere A sphere.
	 * @return The minimum distance between the plane and the surface of the
	 * sphere.
	 */
	public double planeSphere(Plane plane, Ball sphere)
	{
		/*
		 * First find the distance between the plane and the centre of the
		 * sphere. 
		 */
		double out = this.planePoint(plane.normal, plane.d, sphere.getCenter());
		/*
		 * Subtract the rod's radius to find the distance between the plane and
		 * the rod's surface.
		 */
		return out - sphere.getRadius();
	}
	
	/**
	 * \brief Calculate the distance between an infinite plane and a point in
	 * space.
	 * 
	 * <p>This method also sets the internal variable {@link #dP}.</p>
	 * 
	 * @param normal The normal vector of the plane.
	 * @param d The  dot product of the plane's normal vector with a point on
	 * the plane.
	 * @param point The point in space.
	 * @return The minimum distance between the plane and the point.
	 */
	private double planePoint(double[] normal, double d, double[] point)
	{
		Vector.reverseTo(this.dP, normal);
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
	private static double clamp(double a) 
	{
		return Math.max( Math.min(a, 1.0), 0.0 );
	}
}
