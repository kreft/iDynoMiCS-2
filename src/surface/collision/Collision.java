package surface.collision;

import java.util.Collection;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Global;
import instantiable.Instance;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Ball;
import surface.Plane;
import surface.Rod;
import surface.Surface;
import surface.Voxel;
import surface.Surface.Type;
import surface.collision.model.*;

/**
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
	private final Shape _shape;
	/**
	 * Collision variables is an object used to pass variables between the
	 * collision methods as efficiently, limiting required garbage collection
	 */
	private final CollisionVariables _variables;
	/**
	 * store additional collision variables for advanced collision models.
	 */
	private final boolean extend = Global.additional_collision_variables;
	
	/**
	 * Small value to counteract arithmetic errors.
	 */
	static final float EPSILON = 0.00001f;
			
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief Construct a collision iterator with default pull function, but
	 * given push function.
	 * 
	 * TODO ideally we would make this uniform with the instantiable interface
	 * 
	 * @param collisionFunction
	 * @param compartmentShape
	 */
	public Collision(String collisionFunction, 
			String pullFunction, Shape compartmentShape)
	{
		this._shape = compartmentShape;
		this._variables = new CollisionVariables(
				this._shape.getNumberOfDimensions(), 0.0);
		
		setCollisionFunction(collisionFunction);
		
		setAttractionFunction(pullFunction);
		
		this._pullFun.instantiate(null, null);
		this._collisionFun.instantiate(null, null);
	}
	

	/**
	 * \brief Construct a collision iterator with default push and pull 
	 * functions
	 * 
	 * @param aShape
	 */
	public Collision(Shape aShape)
	{
		this(null, null, aShape);
	}
	
	public void setCollisionFunction(String functionClass)
	{
		try {
			this._collisionFun = (CollisionFunction) 
					Instance.getNewThrows(functionClass, null);
		} catch (InstantiationException | IllegalAccessException | 
				ClassNotFoundException | NullPointerException e) {
			if (functionClass == null || functionClass != Global.collision_model)
			{
				setCollisionFunction(Global.collision_model);
				if (Log.shouldWrite(Tier.CRITICAL) && functionClass != null )
					Log.out(Tier.CRITICAL, "Catched erroneous collision "
							+ "function: " + functionClass + " attempting to "
							+ "set " + Global.collision_model + " instead.\n" + 
							e.getMessage());
			}
			else
			{
			this._collisionFun = new DefaultPushFunction();
			Log.out(Tier.CRITICAL, "Catched corrupt collision configuration, "
					+ DefaultPushFunction.class.getSimpleName() +" will be used"
					+ " instead .\n" + e.getMessage());
			}
		}
	}
	
	public void setAttractionFunction(String functionClass)
	{
		try {
			this._pullFun = (CollisionFunction) 
					Instance.getNewThrows(functionClass, null);
		} catch (InstantiationException | IllegalAccessException | 
				ClassNotFoundException | NullPointerException e) {
			if (functionClass != Global.attraction_model)
			{
				setAttractionFunction(Global.attraction_model);
				if (Log.shouldWrite(Tier.CRITICAL) && functionClass != null )
					Log.out(Tier.CRITICAL, "Catched erroneous collision "
							+ "function: " + functionClass + " attempting to "
							+ "set " + Global.attraction_model + " instead.\n" + 
							e.getMessage());
			}
			else
			{
			this._pullFun = new DefaultPullFunction();
			Log.out(Tier.CRITICAL, "Catched corrupt collision configuration, "
					+ DefaultPullFunction.class.getSimpleName() +" will be used"
					+ " instead .\n" + e.getMessage());
			}
		}
		
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
	 * and {@link #interactionVector}. It may also set {@link #s} and {@link #t}, depending on
	 * the surface types.</p>
	 * 
	 * @param a One surface object.
	 * @param b Another surface object.
	 * @param pullDistance The maximum distance between surfaces before they
	 * become attractive.
	 */
	public void collision(Surface a, AspectInterface first, 
			Surface b, AspectInterface second, CollisionVariables var)
	{
		this.distance(a, b, var);
		
		/* 
		 * If the two surfaces overlap, then they should push each other away.
		 */
		if ( var.distance < 0.0 )
		{
			this._collisionFun.interactionForce( var, first, second );
	
			if( var.flip )
			{
				this.applyForce(b, var.interactionVector, var.s);
				Vector.reverseEquals(var.interactionVector);
				this.applyForce(a, var.interactionVector, var.t);
			} 
			else
			{
				this.applyForce(a, var.interactionVector, var.s);
				Vector.reverseEquals(var.interactionVector);
				this.applyForce(b, var.interactionVector, var.t);
			}
		}
		/*
		 * If pull distance is greater than zero, then there may be attraction
		 * between the two surfaces.
		 */
		else if ( var.pullRange > 0.0 )
		{
			if ( var.flip )
				 Vector.reverseEquals(var.interactionVector);
			
			this._pullFun.interactionForce( var, first, second );

			if( var.flip )
			{
				this.applyForce(a, var.interactionVector, var.s);
				this.applyForce(b, Vector.reverse(var.interactionVector), var.t);
			} 
			else
			{
				this.applyForce(b, var.interactionVector, var.s);
				this.applyForce(a, Vector.reverse(var.interactionVector), var.t);
			}
		}
	}

	/**
	 * \brief TODO
	 * 
	 * @param allA
	 * @param allB
	 * @param pullDistance
	 */
	public void collision(Collection<Surface> allA, AspectInterface first, 
			Collection<Surface> allB, AspectInterface second, double pullDistance)
	{
		_variables.setPullRange(pullDistance);
		for ( Surface a : allA )
		{
			for ( Surface b : allB )
			{ 
				this.collision( a, first, b, second, this._variables );
			}
		}
	}
	
	public void collision(Surface a, AspectInterface first, 
			Collection<Surface> allB, AspectInterface second, double pullDistance)
	{
		_variables.setPullRange(pullDistance);
		for ( Surface b : allB )
		{ 
			this.collision( a, first, b, second, this._variables );
		}
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
			((Ball) surf)._point.addToForce( force );
			break;
		case ROD:
			((Rod) surf)._points[0].addToForce(
					Vector.times( force , 1.0 - intersect ) );
			((Rod) surf)._points[1].addToForce(
					Vector.times( force , intersect ) );
			break;
		case PLANE:
			//Skip applying force to domain planes
		default:
			break;
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
		return ( this.distance( a, b ) < margin );
	}
	
	/**
	 * \brief Calculate the distance between two surfaces, subtracting a given
	 * margin.
	 * 
	 * <p>This method always also sets the internal variables {@link #_flip}
	 * and {@link #interactionVector}. It may also set {@link #s} and 
	 * {@link #t}, depending on the surface types.</p>
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
	
	/*
	 * calculate distance without saving orientation data
	 */
	public double distance(Surface a, Surface b)
	{
		_variables.setPullRange( 0.0 );
		CollisionVariables var = this.distance( a, b, _variables );
		return var.distance;
	}
	
	/**
	 * \brief Calculate the distance between two surfaces.
	 * 
	 * <p>This method always also sets the internal variables {@link #_flip}
	 * and {@link #interactionVector}. It may also set {@link #s} and {@link #t}
	 * depending on the surface types.</p>
	 * 
	 * @param a One surface, of unknown type.
	 * @param b Another surface, of unknown type.
	 * @return The minimum distance between the two surfaces.
	 */
	public CollisionVariables distance(Surface a, Surface b, 
			CollisionVariables var)
	{
		/*
		 * First check that both Surfaces exist.
		 */
		if ( a == null || b == null )
			throw new IllegalArgumentException("Null surface given");
		
		switch( a.type() ) {
		case SPHERE:
			var.flip = false;
			return this.assessSphere((Ball) a, b, var);
		case ROD:
			var.flip = false;
			return this.assessRod((Rod) a, b, var); 
		case PLANE:
			var.flip = false;
			return this.assessPlane((Plane) a, b, var);
		case VOXEL:
			var.flip = false;
			return this.assessVoxel((Voxel) a, b, var);
		default:
			System.out.println("WARNING: undefined Surface type");
			return null;
		}

	}
	
	/**
	 * \brief Calculate the distance from a surface to a point.
	 * 
	 * <p>This method always also sets the internal variable 
	 * {@link #interactionVector}. It may also set {@link #s}, depending on the 
	 * surface type.</p>
	 * 
	 * @param a Surface object, of unknown type.
	 * @param p A point in space.
	 * @return The minimum distance from the surface to the point.
	 */
	public double distance(Surface a, double[] p)
	{
		_variables.setPullRange(0.0);
		switch ( a.type() )
		{
		case SPHERE :
			this.spherePoint((Ball) a, p, this._variables);
			return this._variables.distance;
		case ROD :
			this.rodPoint((Rod) a, p, this._variables);
			return this._variables.distance;
		case PLANE:
			this.planePoint((Plane) a, p, this._variables);
			return this._variables.distance;
		default:
			break;
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
	 * <p>This method always also sets the internal variable 
	 * {@link #interactionVector}. It may also set {@link #s}, depending on the 
	 * other surface type.</p>
	 * 
	 * @param plane Surface of an infinite plane.
	 * @param otherSurface Another surface object, of unknown type.
	 * @return The minimum distance between the two surfaces.
	 */
	private CollisionVariables assessPlane(Plane plane, Surface otherSurface, 
			CollisionVariables var)
	{
		if ( otherSurface.type() == Surface.Type.SPHERE )
			return this.planeSphere(plane, (Ball) otherSurface, var);
		else
			return this.planeRod(plane, (Rod) otherSurface, var);
	}
	
	/**
	 * \brief Calculate the distance between a Rod and another surface of
	 * unknown type.
	 * 
	 * <p>This method always also sets the internal variables {@link #s} and 
	 * {@link #interactionVector}. It may also set {@link #t}, depending on the 
	 * other surface type.</p>
	 * 
	 * @param rod Rod surface.
	 * @param otherSurface Another surface object, of unknown type.
	 * @return The minimum distance between the two surfaces.
	 */
	private CollisionVariables assessRod(Rod rod, Surface otherSurface, 
			CollisionVariables var)
	{
		if ( otherSurface.type() == Surface.Type.SPHERE )
			return this.rodSphere(rod, (Ball) otherSurface, var);
		else if ( otherSurface.type() == Surface.Type.VOXEL )
		{
			this.voxelRod(rod, (Voxel) otherSurface, var);
			return var;	
		}
		else
			return this.rodRod(rod, (Rod) otherSurface, var);
	}
	
	private CollisionVariables assessSphere(Ball sphere, Surface otherSurface, 
			CollisionVariables var)
	{
		/* FIXME check surface order in arguments */
		if ( otherSurface.type() == Surface.Type.ROD )
			return this.rodSphere((Rod) otherSurface, sphere, var);
		else if ( otherSurface.type() == Surface.Type.SPHERE )
			return this.sphereSphere(sphere, (Ball) otherSurface, var);
		else if ( otherSurface.type() == Surface.Type.VOXEL )
			return this.voxelSphere((Voxel) otherSurface, sphere, var);
		else if ( otherSurface.type() == Surface.Type.PLANE )
		{
			var.flip = false;
			return this.planeSphere((Plane) otherSurface, sphere, var);
		}
		else
		{
			return null;
		}
	}
	
	private CollisionVariables assessVoxel(Voxel vox, Surface otherSurface, 
			CollisionVariables var)
	{
		/* FIXME check surface order in arguments */
		if ( otherSurface.type() == Surface.Type.ROD )
		{
			this.voxelRod((Rod) otherSurface, vox, var);
			return var;
		}
		else if ( otherSurface.type() == Surface.Type.SPHERE )
			return this.voxelSphere(vox, (Ball) otherSurface, var);
		else
		{
			return null;
		}
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
	private void setPeriodicDistanceVector(double[] a, double[] b, 
			CollisionVariables var)
	{
		this._shape.getMinDifferenceVectorTo(var.interactionVector, a, b);
	}
	
	/**
	 * \brief Calculate the minimum distance between two points in space and
	 * assign it to the var.interactionVector (reduce memory usage)
	 * 
	 * <p>Neither vector is changed by this method.</p>
	 * 
	 * @param a One point in space.
	 * @param b Another point in space.
	 * @return The minmum distance between them.
	 */
	private double[] minDistance(double[] a, double[] b, CollisionVariables var)
	{
		this._shape.getMinDifferenceVectorTo(var.interactionVector, a,b);
		return var.interactionVector;
	}
	
	/**
	 * \brief Point-point distance.
	 * 
	 * <p>This method also sets the internal variable {@link #interactionVector}
	 * It returns the Euclidean norm of {@link #interactionVector}.</p>
	 * 
	 * @param p First point.
	 * @param q Second point.
	 * @return Distance between the two points.
	 */
	private CollisionVariables pointPoint(double[] p, double[] q, 
			CollisionVariables var) 
	{
		this.setPeriodicDistanceVector(p, q, var);
		var.distance = Vector.normEuclidTo(var.distance, var.interactionVector);
		return var;
	}
	
	/**
	 * \brief Calculate the distance from the surface of a sphere to a point.
	 * 
	 * <p>This method also sets the internal variable {@link #interactionVector}
	 * </p>
	 * 
	 * @param a A ball object.
	 * @param p A point in space.
	 * @return The minimum distance from the surface of the sphere to the point.
	 */
	private CollisionVariables spherePoint(Ball a, double[] p, 
			CollisionVariables var)
	{
		/*
		 * First find the distance between the point and the centre of the
		 * sphere. 
		 */
		this.pointPoint(a.getCenter(), p, var);
		/*
		 * Subtract the sphere's radius to find the distance between the point
		 * and the surface of the sphere.
		 */
		var.distance -= a.getRadius();
		return var;
	}
	
	/**
	 * \brief Calculate the distance between two spheres.
	 * 
	 * <p>This method also sets the internal variable {@link #interactionVector}
	 * </p>
	 * 
	 * @param a A ball object representing one sphere.
	 * @param b A ball object representing another sphere.
	 * @return The minimum distance between the surfaces of the two spheres.
	 */
	private CollisionVariables sphereSphere(Ball a, Ball b, 
			CollisionVariables var)
	{
		this.pointPoint(a.getCenter(), b.getCenter(), var);
// NOTE: this is only needed if we implement bounding volumes
//		/* a is around b. */
//		if ( a.bounding )
//		{
//			var.distance = - var.distance + a.getRadius() - b.getRadius();
//			return var;
//		}
//		/* b is around a. */
//		if ( b.bounding )
//		{
//			var.distance = - var.distance  - a.getRadius() + b.getRadius();
//			return var;
//		}
		/* Normal collision. */
		var.distance -= a.getRadius() + b.getRadius();
		/*
		 * additional collision variables
		 */
		if (extend) 
		{ 
			var.radiusEffective = ( a.getRadius() * b.getRadius() ) / 
					( a.getRadius() + b.getRadius() ); 
		}
		return var;
	}
	
	/**
	 * \brief Calculate the distance between an infinite plane and the segment
	 * of a line.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #interactionVector}.</p>
	 * 
	 * @param normal Normal vector of the plane.
	 * @param d 
	 * @param p0 Point in space at one end of the line segment.
	 * @param p1 Point in space at the other end of the line segment.
	 * @return The minimum distance between the plane and the line segment.
	 */
	private CollisionVariables planeLineSeg(double[] normal, double d, 
			double[] p0, double[] p1, CollisionVariables var)
	{
		this.planePoint(normal, d, p0, var);
		double a = Double.valueOf(var.distance);
		this.planePoint(normal, d, p1, var);
		double b = Double.valueOf(var.distance);
		if ( a < b )
		{
			var.t = 0.0;
			var.distance = a;
			return var;
		}
		if ( a > b ) 
		{
			var.t = 1.0;
			var.distance = b;
			return var;
		}
		/* a = b */
		var.t = 0.5;
		return var;
	}
	
	/**
	 * \brief Calculate the distance between an infinite plane and the surface 
	 * of a rod.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #interactionVector}.</p>
	 * 
	 * @param plane Infinite plane.
	 * @param rod Rod surface.
	 * @return Minimum distance between the plane and the rod.
	 */
	private CollisionVariables planeRod(Plane plane, Rod rod, 
			CollisionVariables var)
	{
		/*
		 * First find the distance between the plane and the axis of the rod. 
		 */
		this.planeLineSeg( plane.getNormal(), plane.getD(), 
				rod._points[0].getPosition(), 
				rod._points[1].getPosition(), 
				var);
		/*
		 * Subtract the rod's radius to find the distance between the plane and
		 * the rod's surface.
		 */
		var.distance -= rod.getRadius();
		/*
		 * additional collision variables
		 */
		if (extend) 
		{ 
			var.radiusEffective = rod.getRadius();
		}
		return var;
	}
	
	/**
	 * \brief Calculates the distance between a line segment and a point.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #interactionVector}. It returns the Euclidean norm of 
	 * {@link #interactionVector}.</p>
	 * 
	 * <p>(Ericson 2005, page 127) closest point on line segment to point.</p>
	 * 
	 * @param p0 First point of rod.
	 * @param p1 Second point of rod.
	 * @param q0 Point of sphere.
	 * @return Minimum distance between the line segment and the point.
	 */
	public CollisionVariables linesegPoint(double[] p0, double[] p1, 
			double[] q0, CollisionVariables var) 
	{
		/* ab = p1 - p0 */
		this.setPeriodicDistanceVector(p1, p0, var);
		var.s = Vector.dotProduct( Vector.minus(q0, p0), var.interactionVector);
		var.s /= Vector.normSquare(var.interactionVector);
		var.s  = clamp( var.s );
		/* dP = (ab*s) + p0 - q0 */
		Vector.timesEquals(var.interactionVector, var.s);
		Vector.addEquals(var.interactionVector, p0);
		Vector.minusEquals(var.interactionVector, q0);
		var.distance = Vector.normEuclid(var.interactionVector);
		return var;
	}
	
	/**
	 * \brief Calculate the distance between the surface of a rod and a point
	 * in space.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #interactionVector}.</p>
	 * 
	 * @param aRod A rod surface.
	 * @param p A point in space.
	 * @return The minimum distance between the surface of the rod and the
	 * point.
	 */
	public CollisionVariables rodPoint(Rod aRod, double[] p, 
			CollisionVariables var)
	{
		/*
		 * First find the distance between the axis of the rod and the point. 
		 */
		this.linesegPoint(
				aRod._points[0].getPosition(), 
				aRod._points[1].getPosition(), p, var);
		/*
		 * Subtract the rod's radius to find the distance between the point and
		 * the rod's surface.
		 */
		var.distance -= aRod.getRadius();
		return var;
	}
	
	/**
	 * \brief Calculate the distance between the surfaces of a rod and of a
	 * sphere.
	 * 
	 * <p>This method also sets the internal variables {@link #s} and
	 * {@link #interactionVector}.</p>
	 * 
	 * @param aRod A rod surface.
	 * @param aBall A sphere surface.
	 * @return The minimum distance between the surface of the rod and the
	 * surface of the sphere.
	 */
	public CollisionVariables rodSphere(Rod aRod, Ball aBall, 
			CollisionVariables var)
	{
		/*
		 * First find the distance between the axis of the rod and the centre
		 * of the sphere. 
		 */
		this.linesegPoint(
				aRod._points[0].getPosition(),
				aRod._points[1].getPosition(),
				aBall.getCenter(), var);
		/*
		 * Subtract the radii of both to find the distance between their
		 * surfaces.
		 */
		var.distance -= aRod.getRadius() + aBall.getRadius();
		/*
		 * additional collision variables
		 */
		if (extend) 
		{ 
			var.radiusEffective = ( aRod.getRadius() * aBall.getRadius() ) / 
					( aRod.getRadius() + aBall.getRadius() ); 
		}
		return var;
	}
	
	/**
	 * \brief Calculate the distance between two line segments.
	 * 
	 * <p>This method also sets the internal variables {@link #s}, {@link #t}, 
	 * and {@link #interactionVector}. It returns the Euclidean norm of 
	 * {@link #interactionVector}.</p>
	 * 
	 * <p>(Ericson 2005, page 148) closest point on two line segments.</p>
	 * 
	 * Computes closest points C1 and C2 of S1(s) = P1 + s * ( Q1-P1 ) and 
	 * S2(t) = P2 + t * ( Q2-P2 ), returning s and t. Function result is squared
	 * distance between between S1(s) and S2(t).
	 * 
	 * @param p0 First point of first rod.
	 * @param p1 Second point of first rod.
	 * @param q0 First point of second rod.
	 * @param q1 Second point of second rod.
	 * @return distance between the two line segments.
	 * 
	 * FIXME [Bas Nov2018] there seems to be a periodic test
	 * missing in this one. Check, not 100% sure
	 */
	public CollisionVariables linesegLineseg(double[] p0, double[] p1, 
			double[] q0, double[] q1, CollisionVariables var) 
	{		
		/* direction vector between segment tips */
		double[] r	= this.minDistance(p0, q0, var).clone();
		/* direction vector of first segment */
		double[] d1	= this.minDistance(p1, p0, var).clone();
		/* direction vector of second segement */
		double[] d2	= this.minDistance(q1, q0, var).clone();
		/* squared length of first segment */
		double a 	= Vector.normSquare(d1);
		/* squared length of second segment */
		double e 	= Vector.normSquare(d2);
		double f 	= Vector.dotProduct(d2, r);
		double c 	= Vector.dotProduct(d1, r);
		double b 	= Vector.dotProduct(d1, d2);
		double denominator 	= (a * e) - (b * b);
		
		/* s, t = 0.0 if segments are parallel. */
		if ( denominator == 0.0 )
			var.s = 0.0;
		else
			var.s = clamp( (b*f-c*e) / denominator );	
		
		/*
		 * Compute point on L2 closest to S1(s) using 
		 * t = Dot( (P1 + D1*s) - P2,D2) / Dot(D2,D2) = (b*s + f) / e
		 */
		var.t = (b*var.s + f) / e;
		
		/*
		 * If t in [0,1] (on the line-segment) we can continue. Else we clamp t,
		 * recompute s for the new value of t using 
		 * s = Dot( (P2 + D2*t) - P1 , D1 ) / Dot( D1 ,D1 ) = ( t * b - c ) / a 
		 * and clamp s to [0, 1].
		 */
		if ( var.t < 0.0 ) 
		{
			var.t = 0.0;
			var.s = clamp(-c/a);
		} 
		else if ( var.t > 1.0 ) 
		{
			var.t = 1.0;
			var.s = clamp((b-c)/a);
		}

		/*
		 * the closest point on the first segment is now fraction s of the
		 * length from the first start in the following the direction of the 
		 * segment
		 */
		Vector.timesEquals(d1, var.s);
		Vector.addEquals(d1, p0);
		
		/*
		 * similar for the second point with fraction t
		 */
		Vector.timesEquals(d2, var.t);
		Vector.addEquals(d2, q0);

		/* finally calculate the distance between the two points */
		this.setPeriodicDistanceVector(d1, d2, var);
		var.distance = Vector.normEuclid(var.interactionVector);
		return var;
	}
	
	/**
	 * \brief Calculate the minimum distance between the surfaces of two rods.
	 * 
	 * <p>This method also sets the internal variables {@link #s}, {@link #t}, 
	 * and {@link #interactionVector}.</p>
	 * 
	 * @param a One rod.
	 * @param b Another rod.
	 * @return The minimum distance between the surfaces of the two rods.
	 */
	private CollisionVariables rodRod(Rod a, Rod b, CollisionVariables var)
	{
		/*
		 * First find the distance between the axes of the two rods. 
		 */
		this.linesegLineseg(
				a._points[0].getPosition(),
				a._points[1].getPosition(),
				b._points[0].getPosition(),
				b._points[1].getPosition(), var);
		/*
		 * Subtract the radii of both rods to find the distance between their
		 * surfaces.
		 */
		var.distance -= a.getRadius() + b.getRadius();
		/*
		 * additional collision variables
		 */
		if (extend) 
		{ 
			var.radiusEffective = ( a.getRadius() * b.getRadius() ) / 
					( a.getRadius() + b.getRadius() ); 
		}
		return var;
	}
	
	/**
	 * \brief Distance between a plane and a point.
	 * 
	 * <p>This method also sets the internal variable {@link #interactionVector}
	 * </p>
	 * 
	 * @param plane An infinite plane.
	 * @param point A point in space.
	 * @return The minimum distance between the plane and the point.
	 */
	public CollisionVariables planePoint(Plane plane, double[] point, 
			CollisionVariables var)
	{
		Vector.reverseTo(var.interactionVector, plane.getNormal());
		var.distance = Vector.dotProduct(plane.getNormal(), point)-plane.getD();
		return var;
	}
	
	/**
	 * \brief the closest point on the plane is the position of the point minus 
	 * the distance between the point and the plane in the direction of the
	 * planes normal.
	 * @param normal
	 * @param d
	 * @param point
	 * @return
	 */
	public double[] closestPointOnPlane(double[] normal, double d, 
			double[] point, CollisionVariables var)
	{
		/* Calculate the distance between plane and point */
		this.planePoint(normal, d, point, var);
		/* 
		 * the closest point on the plane is the position of the point minus the
		 * distance between the point and the plane in the direction of the
		 * planes normal.
		 */
		return Vector.minus(point, Vector.times(normal, - var.distance));
	}
	
	/**
	 * \brief Calculate the distance between an infinite plane and the surface
	 * of a sphere.
	 * 
	 * <p>This method also sets the internal variable {@link #interactionVector}
	 * </p>
	 * 
	 * @param plane An infinite plane.
	 * @param sphere A sphere.
	 * @return The minimum distance between the plane and the surface of the
	 * sphere.
	 */
	public CollisionVariables planeSphere(Plane plane, Ball sphere, 
			CollisionVariables var)
	{
		/*
		 * First find the distance between the plane and the centre of the
		 * sphere. 
		 */
		this.planePoint(plane.getNormal(),plane.getD(),sphere.getCenter(), var);
		/*
		 * Subtract the rod's radius to find the distance between the plane and
		 * the rod's surface.
		 */
		var.distance -= sphere.getRadius();
		/*
		 * additional collision variables
		 */
		if (extend) 
		{ 
			var.radiusEffective = sphere.getRadius(); 
		}
		return var;
	}
	
	/**
	 * \brief Calculate the distance between a normalized infinite plane and a 
	 * point in space.
	 * 
	 * <p>This method also sets the internal variable {@link #interactionVector}
	 * </p>
	 * 
	 * @param normal The normal vector of the plane.
	 * @param d The  dot product of the plane's normal vector with a point on
	 * the plane.
	 * @param point The point in space.
	 * @return The minimum distance between the plane and the point.
	 */
	private CollisionVariables planePoint(double[] normal, double d, 
			double[] point, CollisionVariables var)
	{
		/* store the direction vector */
		Vector.reverseTo(var.interactionVector, normal);
		/* calculate the distance between a point and a normalized plane */
		var.distance = Vector.dotProduct(normal, point) - d;
		return var;
	}
	
	/**
	 * TODO \brief
	 * 
	 * FIXME this method does not seem to be adjusted to account for periodic
	 * boundaries
	 */
	private CollisionVariables voxelSphere(Voxel voxel, Ball sphere, 
			CollisionVariables var)
	{
		double[] p = Vector.copy( sphere._point.getPosition() );
		for(int i=0; i < p.length ; i++) 
		{ 
			p[i] = Math.max( p[i], voxel.getLower()[i] );
			p[i] = Math.min( p[i], voxel.getHigher()[i] );
		}
		return this.spherePoint(sphere, p, var);
	}

	/**
	 * TODO real-time collsion detection pp 229
	 * @param rod
	 * @param voxel
	 * @param t 
	 * @return
	 */
	private boolean voxelRod(Rod rod, Voxel voxel, CollisionVariables t)
	{
		// Compute the AABB resulting from expanding b by sphere radius r 
//		AABB e = b; 
		double[] emin = voxel.getLower();
		double[] emax = voxel.getHigher();
		double r = rod.getRadius();
		Voxel e = new Voxel(Vector.minus(emin, r), Vector.add(emax, r));
//		e.min.x -= s.r; e.min.y -= s.r; e.min.z -= s.r; 
//		e.max.x += s.r; e.max.y += s.r; e.max.z += s.r;
		// Intersect ray against expanded AABB e. Exit with no intersection if ray 
		// misses e, else get intersection point p and time t as result 
		
		double[] p = new double[emin.length];
		if ( !intersectRayAABB(rod._points[0].getPosition(), 
				Vector.minus(rod._points[1].getPosition(), 
				rod._points[0].getPosition()), e, t.t, p) || t.t > 1.0f ) 
			return false;
		// Compute which min and max faces of b the intersection point p lies 
		// outside of. Note, u and v cannot have the same bits set and 
		// they must have at least one bit set among them 
		int u=0,v=0; 
			if (p[0] < voxel.getLower()[0]) u |= 1; 
			if (p[0] > voxel.getHigher()[0]) v |= 1; 
		if (p.length > 1)
		{
			if (p[1] < voxel.getLower()[1]) u |= 2; 
			if (p[1] > voxel.getHigher()[1]) v |= 2; 
		}
		if (p.length > 2)
		{
			if (p[2] < voxel.getLower()[2]) u |= 4; 
			if (p[2] > voxel.getHigher()[2]) v |= 4;
		}
		// ‘Or’ all set bits together into a bit mask (note: here u+v==u|v) 
		int m = u + v;
		// Define line segment [c, c+d] specified by the sphere movement 
		//Segment seg(s.c, s.c + d);
		// --> Rod
		
		// If all 3 bits set (m == 7) then p is in a vertex region 
		if (m == 7) { 
			// Must now intersect segment [c, c+d] against the capsules of the three 
			// edges meeting at the vertex and return the best time, if one or more hit 
			float tmin = Float.MAX_VALUE; 
			if ( intersectSegmentCapsule(Corner(voxel, bool(v)), 
					Corner(voxel, bool(v ^ 1)), rod._points[0].getPosition(), 
					rod._points[1].getPosition(), rod.getRadius(), t.t) ) 
				tmin = (float) Math.min(t.t, tmin);
			if ( intersectSegmentCapsule(Corner(voxel, bool(v)), 
					Corner(voxel, bool(v ^ 2)), rod._points[0].getPosition(), 
					rod._points[1].getPosition(), rod.getRadius(), t.t) ) 
				tmin = (float) Math.min(t.t, tmin);
			if ( intersectSegmentCapsule(Corner(voxel, bool(v)), 
					Corner(voxel, bool(v ^ 4)), rod._points[0].getPosition(), 
					rod._points[1].getPosition(), rod.getRadius(), t.t) ) 
				tmin = (float) Math.min(t.t, tmin);
			if ( tmin == Float.MAX_VALUE ) 
				return false; // No intersection
		t.t = tmin; 
		return true;
		// Intersection at time t == tmin 
		}
		// If only one bit set in m, then p is in a face region 
		if ((m & (m - 1)) == 0) { 
			// Do nothing. Time t from intersection with 
			// expanded box is correct intersection time 
			return true;
		}
		// p is in an edge region. Intersect against the capsule at the edge 
		return intersectSegmentCapsule(Corner(voxel, bool(u ^ 7)), 
				Corner(voxel, bool(v)), rod._points[0].getPosition(), 
				rod._points[1].getPosition(), rod.getRadius(), t.t);
	}
	
	private boolean bool(int n)
	{
		return n != 0;
	}
	
	//Support function that returns the AABB vertex with index n 
	private double[] Corner(Voxel b, boolean n) 
	{
		double[] p = new double[b.getHigher().length]; 
		p[0] = ((n & true) ? b.getHigher()[0] : b.getLower()[0]); 
		p[1] = ((n & true) ? b.getHigher()[1] : b.getLower()[1]); 
		p[2] = ((n & true) ? b.getHigher()[2] : b.getLower()[2]); 
		return p;
	}
	
	/**
	 * TODO Real-time collision detection pp 180
	 * 
	 * // Intersect ray R(t)=p+t*d against AABB a. When intersecting, 
	 * // return intersection distance tmin and point q of intersection
	 * 
	 * @param p
	 * @param d
	 * @param a
	 * @param tmin
	 * @param q
	 * @return
	 */
	private boolean intersectRayAABB( double[] p, double[] d, Voxel a, double tmin, 
			double[] q)
	{
		// set to -FLT_MAX to get first hit on line
		tmin = 0.0f;  
		// set to max distance ray can travel (for segment)
		float tmax = Float.MAX_VALUE; 
		// For all three slabs 
		for(int i=0; i<3; i++) 
		{ 
			if ( Math.abs(d[i]) < EPSILON) 
			{ 
				// Ray is parallel to slab. No hit if origin not within slab 
				if (p[i] < a.getLower()[i] || p[i] > a.getHigher()[i]) 
					return false;
			} 
			else
			{ 
				// Compute intersection t value of ray with near and far plane of slab 
				float ood = 1.0f / (float) d[i]; 
				float t1 = (float) ((a.getLower()[i] - p[i]) * ood); 
				float t2 = (float) ((a.getLower()[i] - p[i]) * ood); 
				// Make t1 be intersection with near plane, t2 with far plane 
				if (t1 > t2) 
					Swap(t1, t2); 
				// Compute the intersection of slab intersection intervals 
				if (t1 > tmin) 
					tmin = t1; 
				if (t2 > tmax) 
					tmax = t2; 
				// Exit with no collision as soon as slab intersection becomes empty 
				if (tmin > tmax) 
					return false;
			} 
		}
		// Ray intersects all 3 slabs. Return point (q) and intersection t value (tmin) 
	//		q=p+d* tmin;
			q = Vector.add(Vector.times(d, tmin), p); 
			return true;

	}
	
	/**
	 * TODO Real-time collision detection pp 197
	 * 
	 * Intersect segment S(t)=sa+t(sb-sa), 0<=t<=1 against cylinder specified by p, q and r
	 * @return
	 */
	private boolean intersectSegmentCylinder(double[] sa, double[] sb, double[] p, double[] q, float r, float t)
	{
		double[] d = Vector.minus(q, p), 
				m = Vector.minus(sa,p), 
				n = Vector.minus(sb,sa); 
		double md = Vector.dotProduct(m, d); 
		double nd = Vector.dotProduct(n, d); 
		double dd = Vector.dotProduct(d, d); 
		// Test if segment fully outside either endcap of cylinder 
		if (md < 0.0f && md + nd < 0.0f) 
			return false; 
		// Segment outside ’p’ side of cylinder 
		if (md > dd && md + nd > dd) 
			return false; 
		// Segment outside ’q’ side of cylinder 
		double nn = Vector.dotProduct(n, n); 
		double mn = Vector.dotProduct(m, n); 
		double a = dd * nn - nd * nd;
		float k = (float) Vector.dotProduct(m, m) - r*r; 
		float c = (float) (dd * k - md * md); 
		if ( Math.abs(a) < EPSILON) { 
			// Segment runs parallel to cylinder axis 
			if (c > 0.0f) 
				return false;
		// ’a’ and thus the segment lie outside cylinder
		// Now known that segment intersects cylinder; figure out how it intersects 
			if (md < 0.0f)
				t = (float) (-mn/nn);
		// Intersect segment against ’p’ endcap
		else if (md > dd)
			t = (float) ((nd-mn)/nn); 
			// Intersect segment against ’q’ endcap 
		else 
			t = 0.0f;
		// ’a’ lies inside cylinder
		return true;
		}
		
		float b = (float) (dd*mn-nd*md); 
		float discr=(float) (b*b - a*c); 
		if (discr < 0.0f) 
			return false;
		// No real roots; no intersection
		
		t = (float) ((-b - Math.sqrt(discr)) / a); 
		if (t < 0.0f || t > 1.0f) 
			return false;
		// Intersection lies outside segment
		if( md+t*nd< 0.0f) { 
			// Intersection outside cylinder on ’p’ side 
			if (nd <= 0.0f) 
				return false;
		// Segment pointing away from endcap
		t = (float) (-md / nd); 
		// Keep intersection if Dot(S(t) - p, S(t) - p) <= r∧2 
		return k+2*t*(mn+t*nn)<= 0.0f;
		} 
		else if (md+t*nd>dd)
		{ 
			// Intersection outside cylinder on ’q’ side 
			if (nd >= 0.0f) return false; 
			// Segment pointing away from endcap 
			t = (float) ((dd - md) / nd); 
			// Keep intersection if Dot(S(t) - q, S(t) - q) <= r∧2 
			return k+dd-2*md+t*(2*(mn-nd)+t*nn)<= 0.0f;
		}
		// Segment intersects cylinder between the endcaps; t is correct 
		return true;
	}
	
	/**
	 * TODO Real-time collision detection pp 197
	 * slight adjust of IntersectSegmentCylinder
	 * @return
	 */
	private boolean intersectSegmentCapsule(double[] sa, double[] sb, double[] p, double[] q, double r, double t)
	{
		double[] d = Vector.minus(q, p), 
				m = Vector.minus(sa,p), 
				n = Vector.minus(sb,sa); 
		double md = Vector.dotProduct(m, d); 
		double nd = Vector.dotProduct(n, d); 
		double dd = Vector.dotProduct(d, d); 
		// Test if segment fully outside either endcap of cylinder 
//		if (md < 0.0f && md + nd < 0.0f) 
//			return false; 
//		// Segment outside ’p’ side of cylinder 
//		if (md > dd && md + nd > dd) 
//			return false; 
//		// Segment outside ’q’ side of cylinder 
		double nn = Vector.dotProduct(n, n); 
		double mn = Vector.dotProduct(m, n); 
		double a = dd * nn - nd * nd;
		float k = (float) ((float) Vector.dotProduct(m, m) - r*r); 
		float c = (float) (dd * k - md * md); 
		if ( Math.abs(a) < EPSILON) { 
			// Segment runs parallel to cylinder axis 
			if (c > 0.0f) 
				return false;
		// ’a’ and thus the segment lie outside cylinder
		// Now known that segment intersects cylinder; figure out how it intersects 
			if (md < 0.0f)
				t = (float) (-mn/nn);
		// Intersect segment against ’p’ endcap
		else if (md > dd)
			t = (float) ((nd-mn)/nn); 
			// Intersect segment against ’q’ endcap 
		else 
			t = 0.0f;
		// ’a’ lies inside cylinder
		return true;
		}
		
		float b = (float) (dd*mn-nd*md); 
		float discr=(float) (b*b - a*c); 
		if (discr < 0.0f) 
			return false;
		// No real roots; no intersection
		
		t = (float) ((-b - Math.sqrt(discr)) / a); 
		if (t < 0.0f || t > 1.0f) 
			return false;
		// Intersection lies outside segment
		if( md+t*nd< 0.0f) { 
			// Intersection outside cylinder on ’p’ side 
			if (nd <= 0.0f) 
				return false;
		// Segment pointing away from endcap
		t = (float) (-md / nd); 
		// Keep intersection if Dot(S(t) - p, S(t) - p) <= r∧2 
		return k+2*t*(mn+t*nn)<= 0.0f;
		} 
		else if (md+t*nd>dd)
		{ 
			// Intersection outside cylinder on ’q’ side 
			if (nd >= 0.0f) return false; 
			// Segment pointing away from endcap 
			t = (float) ((dd - md) / nd); 
			// Keep intersection if Dot(S(t) - q, S(t) - q) <= r∧2 
			return k+dd-2*md+t*(2*(mn-nd)+t*nn)<= 0.0f;
		}
		// Segment intersects cylinder between the endcaps; t is correct 
		return true;
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
	
	private void Swap(float t1, float t2) 
	{
		float temp = t1;
		t1 = t2;
		t2 = temp;
	}
}
