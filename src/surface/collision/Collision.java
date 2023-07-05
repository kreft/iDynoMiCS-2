package surface.collision;

import java.util.Collection;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Global;
import instantiable.Instance;
import linearAlgebra.Vector;
import shape.Shape;
import surface.Ball;
import surface.Plane;
import surface.Rod;
import surface.Surface;
import surface.Voxel;
import surface.collision.model.DefaultPullFunction;
import surface.collision.model.DefaultPushFunction;

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
	static final double EPSILON = 0.00001;
			
	
	/* ***********************************************************************
	 * Constructors and constructor helper methods
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
	
	/**
	 * Set a standard collision function from string, if this fails (for
	 * example if the user defined function does not exist) write a critical
	 * warning and set the default function instead.
	 * @param functionClass
	 */
	public void setCollisionFunction(String functionClass)
	{
		try {
			this._collisionFun = (CollisionFunction) 
					Instance.getNewThrows(functionClass, null);
		} catch ( Exception e ) {
			if (functionClass == null || 
					functionClass != Global.collision_model)
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
	
	/**
	 * Set a standard attraction function from string, if this fails (for
	 * example if the user defined function does not exist) write a critical
	 * warning and set the default function instead.
	 * @param functionClass
	 */
	public void setAttractionFunction(String functionClass)
	{
		try {
			this._pullFun = (CollisionFunction) 
					Instance.getNewThrows(functionClass, null);
		} catch ( Exception e ) {
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

	public void resetOverlap() 	{
		this._variables.resetOverlap();
	}

	public double maxOverlap() 	{
		return this._variables.maxOverlap();
	}

	public void updateOverlap( double overlap ) {
		this._variables.setDistance( overlap );
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
	 * and {@link #interactionVector}. It may also set {@link #s} and 
	 * {@link #t}, depending on the surface types.</p>
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
		
		if ( Double.isNaN(var.interactionVector[0]))
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" detected unidentified number (NaN) " + var.interactionVector[0]);
		/* 
		 * If the two surfaces overlap, then they should push each other away.
		 */
		if ( var.getDistance() < 0.0 )
		{
			this._collisionFun.interactionForce( var, first, second );
			if ( Double.isNaN(var.interactionVector[0]))
				Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
						" detected unidentified number (NaN) " + var.interactionVector[0]);
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
		else if ( var.margin > 0.0 )
		{
			if ( var.flip )
				 Vector.reverseEquals(var.interactionVector);
			
			this._pullFun.interactionForce( var, first, second );

			if( var.flip )
			{
				this.applyForce(a, var.interactionVector, var.s);
				this.applyForce(b, Vector.reverse(var.interactionVector),var.t);
			} 
			else
			{
				this.applyForce(b, var.interactionVector, var.s);
				this.applyForce(a, Vector.reverse(var.interactionVector),var.t);
			}
		}
	}

	/**
	 * \brief Check all surfaces from collection A against all surfaces from 
	 * collection B.
	 * 
	 * @param allA
	 * @param allB
	 * @param pullDistance
	 */
	public void collision(Collection<Surface> allA, AspectInterface first, 
			Collection<Surface> allB, AspectInterface second, double 
			pullDistance)
	{
		_variables.setMargin(pullDistance);
		for ( Surface a : allA )
		{
			for ( Surface b : allB )
			{ 
				this.collision( a, first, b, second, this._variables );
			}
		}
	}
	
	/**
	 * \brief Check surface A against all surfaces from collection B.
	 * 
	 * @param allA
	 * @param allB
	 * @param pullDistance
	 */
	public void collision(Surface a, AspectInterface first, 
			Collection<Surface> allB, AspectInterface second, 
			double pullDistance)
	{
		_variables.setMargin(pullDistance);
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
		if ( Double.isNaN(force[0]))
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" detected unidentified number (NaN) ");
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
	 * Assess distance of between two surfaces.
	 * **********************************************************************/
	
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
	
	/**
	 * calculate distance without saving orientation data
	 */
	public double distance(Surface a, Surface b)
	{
		_variables.setMargin( 0.0 );
		CollisionVariables var = this.distance( a, b, _variables );
		return var.getDistance();
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
			throw new IllegalArgumentException(this.getClass().getSimpleName() +
					" Null surface given");
		
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
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered undefined surface assessment." );
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
		_variables.setMargin(0.0);
		switch ( a.type() )
		{
		case SPHERE :
			this.spherePoint( (Ball) a, p, this._variables );
			return this._variables.getDistance();
		case ROD :
			this.rodPoint( (Rod) a, p, this._variables );
			return this._variables.getDistance();
		case PLANE:
			this.planePoint( (Plane) a, p, this._variables );
			return this._variables.getDistance();
		default:
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered undefined surface assessment, returning 0.");
			break;
		}
		return 0.0;
	}
	
	/**
	 * \brief Calculate the distance between a sphere and another surface of
	 * unknown type.
	 * 
	 * <p>This method always also sets the internal variables {@link #s} and 
	 * {@link #interactionVector}. It may also set {@link #t}, depending on the 
	 * other surface type.</p>
	 * 
	 * @param Sphere surface.
	 * @param otherSurface Another surface object, of unknown type.
	 * @return CollisionVariables including the minimum distance between the 
	 * two surfaces.
	 */
	private CollisionVariables assessSphere(Ball sphere, Surface otherSurface, 
			CollisionVariables var)
	{
		/* FIXME check surface order in arguments */
		switch( otherSurface.type() ) 
		{
		case SPHERE:
			return this.sphereSphere(sphere, (Ball) otherSurface, var);
		case ROD:
			var.flip = true;
			return this.rodSphere((Rod) otherSurface, sphere, var);
		case PLANE:
			var.flip = false;
			return this.planeSphere((Plane) otherSurface, sphere, var);
		case VOXEL:
			return this.voxelSphere((Voxel) otherSurface, sphere, var);
		default:
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered unimplemented surface assessment.");
			return null;
		}
	}
	
	/**
	 * \brief Calculate the distance between a Rod and another surface of
	 * unknown type.
	 * 
	 * <p>This method always also sets the internal variables {@link #s} and 
	 * {@link #interactionVector}. It may also set {@link #t}, depending on the 
	 * other surface type.</p>
	 * 
	 * @param Rod surface
	 * @param otherSurface Another surface object, of unknown type.
	 * @return The minimum distance between the two surfaces.
	 */
	private CollisionVariables assessRod(Rod rod, Surface otherSurface, 
			CollisionVariables var)
	{
		switch( otherSurface.type() ) 
		{
		case SPHERE:
			return this.rodSphere( rod, (Ball) otherSurface, var );
		case ROD:
			return this.rodRod( rod, (Rod) otherSurface, var );
		case PLANE:
			return this.planeRod( (Plane) otherSurface, rod, var );
		default:
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered unimplemented surface assessment.");
			return null;
		}
	}
	
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
		switch( otherSurface.type() ) 
		{
		case SPHERE:
			return this.planeSphere(plane, (Ball) otherSurface, var);
		case ROD:
			return this.planeRod(plane, (Rod) otherSurface, var);
		default:
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered unimplemented surface assessment.");
			return null;
		}
	}
	
	/**
	 * \brief Calculate the distance between a voxel and another surface of
	 * unknown type.
	 * 
	 * <p>This method always also sets the internal variables {@link #s} and 
	 * {@link #interactionVector}. It may also set {@link #t}, depending on the 
	 * other surface type.</p>
	 * 
	 * @param Voxel surface.
	 * @param otherSurface Another surface object, of unknown type.
	 * @return CollisionVariables including the minimum distance between the 
	 * two surfaces.
	 */
	private CollisionVariables assessVoxel(Voxel vox, Surface otherSurface, 
			CollisionVariables var)
	{
		/* FIXME check surface order in arguments */
		/* For voxel rod distance have a look at: 
		 * for rod voxel note that output par t defines the fraction of the 
		 * vector between rod1 and rod2 where the rod is closest to the voxel,
		 * from that you can do a voxel point distance.*/
		switch( otherSurface.type() ) 
		{
		case SPHERE:
			return this.voxelSphere(vox, (Ball) otherSurface, var);
		default:
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered unimplemented surface assessment.");
			return null;
		}
	}
	
	/* ***********************************************************************
	 * Detect intersection of two surfaces.
	 * **********************************************************************/

	/**
	 * \brief Check for intersection (overlap) between surface a and surface b.
	 * 
	 * TODO: some surface types may be more efficiently evaluated by switching
	 * from the more expensive assess distance < margin to intersect paradigms.
	 * 
	 * @param a One surface, of unknown type.
	 * @param b Another surface, of unknown type.
	 * @param margin Minimum distance between the two surfaces.
	 * @return True if the distance between the two surfaces is less than
	 * the margin given, otherwise false.
	 */
	public boolean intersect(Surface a, Surface b, double margin)
	{
		_variables.setMargin( margin );
		switch( a.type() ) 
		{
		case SPHERE:
			_variables.flip = false;
			this.assessSphere((Ball) a, b, _variables);
			return _variables.getDistance() < margin;
		case ROD:
			_variables.flip = false;
			return this.intersectRod((Rod) a, b, _variables); 
		case PLANE:
			_variables.flip = false;
			this.assessPlane((Plane) a, b, _variables);
			return _variables.getDistance() < margin;
		case VOXEL:
			_variables.flip = false;
			return this.intersectVoxel((Voxel) a, b, _variables);
		default:
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered undefined surface assessment." );
			return false;
		}
	}
	
	/**
	 * \brief: Assess whether rod is intersecting (overlap) with other surface.
	 * @param rod
	 * @param otherSurface
	 * @param var
	 * @return
	 */
	private boolean intersectRod(Rod rod, Surface otherSurface, 
			CollisionVariables var)
	{
		switch( otherSurface.type() ) 
		{
		case SPHERE:
			this.rodSphere(rod, (Ball) otherSurface, var);
			return var.getDistance() < var.margin;
		case ROD:
			this.rodRod(rod, (Rod) otherSurface, var);
			return var.getDistance() < var.margin;
		case PLANE:
			this.planeRod((Plane) otherSurface, rod, var);
			return var.getDistance() < var.margin;
		case VOXEL:
			return this.voxelRodIntersection(rod, (Voxel) otherSurface, var);
		default:
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered unimplemented surface assessment.");
			return false;
		}
	}
	
	/**
	 * \brief: Assess whether vox is intersecting (overlap) with other surface.
	 * @param vox
	 * @param otherSurface
	 * @param CollisionVariables object
	 * @return true if surfaces intersect
	 */
	private boolean intersectVoxel(Voxel vox, Surface otherSurface, 
			CollisionVariables var)
	{
		/* FIXME check surface order in arguments */
		switch( otherSurface.type() ) 
		{
		case SPHERE:
			this.voxelSphere(vox, (Ball) otherSurface, var);
			return var.getDistance() < var.margin;
		case ROD:
			return this.voxelRodIntersection((Rod) otherSurface, vox, var);
		default:
			Log.out( Tier.CRITICAL, this.getClass().getSimpleName() +
					" encountered unimplemented surface assessment.");
			return false;
		}
	}
	
	/* ***********************************************************************
	 * Distance and intersection evaluation methods for numerous surfaces
	 ************************************************************************/

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
		var.setDistance(Vector.normEuclidTo(var.getDistance(), var.interactionVector));
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
		var.setDistance(var.getDistance() - a.getRadius());
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
		
		/*  NOTE: The following commented code would be required to create
		 * bounding surfaces for non cartesian domain shapes for example a
		 * spherical or cylindrical domain.
		if ( a.bounding ) {
			var.distance = - var.distance + a.getRadius() - b.getRadius();
			return var;
		}
		if ( b.bounding ) {
			var.distance = - var.distance  - a.getRadius() + b.getRadius();
			return var;
		}
		 */
		
		/* Normal collision. */
		var.setDistance(var.getDistance() - (a.getRadius() + b.getRadius()));
		/* additional collision variables */
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
		int i;
		if( p0[1] > 28.0)
		{
			i=0;
		}
		this.planePoint(normal, d, p0, var);
		double a = Double.valueOf(var.getDistance());
		this.planePoint(normal, d, p1, var);
		double b = Double.valueOf(var.getDistance());
		if ( a < b )
		{
			var.t = 0.0;
			var.setDistance(a);
			return var;
		}
		if ( a > b ) 
		{
			var.t = 1.0;
			var.setDistance(b);
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
		/* First find the distance between the plane and the axis of the rod. */
		this.planeLineSeg( plane.getNormal(), plane.getD(), 
				rod._points[0].getPosition(), 
				rod._points[1].getPosition(), 
				var);
		/* Subtract the rod's radius to find the distance between the plane and
		 * the rod's surface. */
		var.setDistance(var.getDistance() - rod.getRadius());
		/* additional collision variables */
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
		double[] sp0 = this._shape.getNearestShadowPoint(p0,q0);
		double[] sp1 = this._shape.getNearestShadowPoint(p1,q0);
		/* ab = p1 - p0 */
		this.setPeriodicDistanceVector(sp1, sp0, var);
		var.s = Vector.dotProduct( Vector.minus(q0, sp0), var.interactionVector);
		var.s /= Vector.normSquare(var.interactionVector);
		var.s  = clamp( var.s );
		/* dP = (ab*s) + p0 - q0 */
		Vector.timesEquals(var.interactionVector, var.s);
		Vector.addEquals(var.interactionVector, sp0);
		Vector.minusEquals(var.interactionVector, q0);
		var.setDistance(Vector.normEuclid(var.interactionVector));
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
		/* First find the distance between the axis of the rod and the point. */
		this.linesegPoint(
				aRod._points[0].getPosition(), 
				aRod._points[1].getPosition(), p, var);
		/* Subtract the rod's radius to find the distance between the point and
		 * the rod's surface. */
		var.setDistance(var.getDistance() - aRod.getRadius());
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
		var.setDistance(var.getDistance() - (aRod.getRadius() + aBall.getRadius()));
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
		/* direction vector of second segment */
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
		/* Compute point on L2 closest to S1(s) using 
		 * t = Dot( (P1 + D1*s) - P2,D2) / Dot(D2,D2) = (b*s + f) / e */
		var.t = (b*var.s + f) / e;
		/*If t in [0,1] (on the line-segment) we can continue. Else we clamp t,
		 * recompute s for the new value of t using 
		 * s = Dot( (P2 + D2*t) - P1 , D1 ) / Dot( D1 ,D1 ) = ( t * b - c ) / a 
		 * and clamp s to [0, 1]. */
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
		/* the closest point on the first segment is now fraction s of the
		 * length from the first start in the following the direction of the 
		 * segment. */
		Vector.timesEquals(d1, var.s);
		Vector.addEquals(d1, p0);
		/* similar for the second point with fraction t */
		Vector.timesEquals(d2, var.t);
		Vector.addEquals(d2, q0);
		/* finally calculate the distance between the two points */
		this.setPeriodicDistanceVector(d1, d2, var);
		var.setDistance(Vector.normEuclid(var.interactionVector));
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
		/* First find the distance between the axes of the two rods.  */
		this.linesegLineseg(
				a._points[0].getPosition(),
				a._points[1].getPosition(),
				b._points[0].getPosition(),
				b._points[1].getPosition(), var);
		/* Subtract the radii of both rods to find the distance between their
		 * surfaces.  */
		var.setDistance(var.getDistance() - (a.getRadius() + b.getRadius()));
		/* additional collision variables */
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
		var.setDistance(Vector.dotProduct(plane.getNormal(), point)-plane.getD());
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
		return Vector.minus(point, Vector.times(normal, -var.getDistance()));
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
		var.setDistance(var.getDistance() - sphere.getRadius());
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
		var.setDistance(Vector.dotProduct(normal, point) - d);
		return var;
	}
	
	/**
	 * TODO \brief
	 * 
	 * TODO is this method correct? maybe easier to just clamp the box
	 * 
	 * real-time collision detection pp 130~132
	 * FIXME this method does not seem to be adjusted to account for periodic
	 * boundaries
	 */
	private CollisionVariables voxelSphere(Voxel voxel, Ball sphere, 
			CollisionVariables var)
	{
		var = voxelPoint(voxel, sphere._point.getPosition(), var);
		var.setDistance(var.getDistance() - sphere.getRadius());
		return var;
	}
	
	private CollisionVariables voxelPoint(Voxel voxel, double[] point,
			CollisionVariables var)
	{
		var.setDistance(0.0); double t,v = 0.0;
		for(int i=0; i < voxel.getLower().length ; i++) 
		{ 
			double[] p = this._shape.getNearestShadowPoint( point, 
					voxel.getLower() );
			t = voxel.getLower()[i];
			v = p[i];
			if (v < t)
				var.setDistance(var.getDistance() + ( t - v ) * ( t - v ));
			t = voxel.getHigher()[i];
			if (v > t)
				var.setDistance(var.getDistance() + ( v - t ) * ( v - t ));
		}
		var.setDistance(Math.sqrt(var.getDistance()));
		return var;
		
	}
	
	/**
	 * TODO real-time collision detection pp 229
	 * @param rod
	 * @param voxel
	 * @param var 
	 * @return
	 */
	private boolean voxelRodIntersection(Rod rod, Voxel voxel, CollisionVariables var)
	{
		/* Compute the AABB resulting from expanding b by sphere radius r */
		double[] emin = voxel.getLower();
		double[] emax = voxel.getHigher();
		double r = rod.getRadius() + var.margin;
		double[] p0 = this._shape.getNearestShadowPoint(
				rod._points[0].getPosition(), voxel.getLower());
		double[] p1 = this._shape.getNearestShadowPoint(
				rod._points[1].getPosition(), voxel.getLower());
		Rod periodicShadow = new Rod(p0, p1, r);
		Voxel e = new Voxel(Vector.minus(emin, r), Vector.add(emax, r));
		/* Intersect ray against expanded AABB e. Exit with no intersection if 
		 * ray misses e, else get intersection point p and time t as result */
		
		var = intersectRayAABB(periodicShadow._points[0].getPosition(), 
				periodicShadow._points[1].getPosition(), e, var);
		if (var.getDistance() == Double.MAX_VALUE || var.t > 1.0 )
			return false;
		/* Compute which min and max faces of b the intersection point p lies 
		 * outside of. Note, u and v cannot have the same bits set and 
		 * they must have at least one bit set among them */
		int u = 0;
		int v = 0; 
		if (var.interactionVector[0] < voxel.getLower()[0]) 
			u |= 1; 
		if (var.interactionVector[0] > voxel.getHigher()[0]) 
			v |= 1; 
		if (var.interactionVector.length > 1)
		{
			if (var.interactionVector[1] < voxel.getLower()[1]) 
				u |= 2; 
			if (var.interactionVector[1] > voxel.getHigher()[1]) 
				v |= 2; 
		}
		if (var.interactionVector.length > 2)
		{
			if (var.interactionVector[2] < voxel.getLower()[2]) 
				u |= 4; 
			if (var.interactionVector[2] > voxel.getHigher()[2]) 
				v |= 4;
		}
		/* ‘Or’ all set bits together into a bit mask (note: here u+v==u|v) */
		int m = u + v;
		/* Define line segment [c, c+d] specified by the sphere movement 
		 * Segment seg(s.c, s.c + d);
		 * --> Rod */
		int l = 3;
		if ( voxel.getLower().length == 3 )
			l = 7;
		/* If all 3 bits set (m == 7) then p is in a vertex region */
		if (m == l) { 
		/* Must now intersect segment [c, c+d] against the capsules of the three 
		 * edges meeting at the vertex and return the best time, if one or more 
		 * hit. */
			double tmin = Float.MAX_VALUE; 
			if ( intersectSegmentCapsule(Corner(voxel, v), Corner(voxel, v ^ 1),
					periodicShadow._points[0].getPosition(), 
					periodicShadow._points[1].getPosition(), 
					periodicShadow.getRadius(), var) ) 
				tmin = Math.min(var.t, tmin);
			if ( intersectSegmentCapsule(Corner(voxel, v), Corner(voxel, v ^ 2),
					periodicShadow._points[0].getPosition(), 
					periodicShadow._points[1].getPosition(), 
					periodicShadow.getRadius(), var) ) 
				tmin = Math.min(var.t, tmin);
			if ( intersectSegmentCapsule(Corner(voxel, v), Corner(voxel, v ^ 4),
					periodicShadow._points[0].getPosition(), 
					periodicShadow._points[1].getPosition(), 
					periodicShadow.getRadius(), var) ) 
				tmin = Math.min(var.t, tmin);
			if ( tmin == Float.MAX_VALUE ) 
				return false; /* No intersection */
		var.t = tmin; 
		return true;
		/* Intersection at time t == tmin */
		}
		/* If only one bit set in m, then p is in a face region */
		if ((m & (m - 1)) == 0) { 
			/* Do nothing. Time t from intersection with 
			 * expanded box is correct intersection time */
			return true;
		}
		/* p is in an edge region. Intersect against the capsule at the edge */
		return intersectSegmentCapsule( Corner(voxel, u ^ l), Corner(voxel, v), 
				periodicShadow._points[0].getPosition(), 
				periodicShadow._points[1].getPosition(), 
				periodicShadow.getRadius(), var);
	}
	
	/**
	 * TODO Real-time collision detection pp 180
	 * 
	 * Intersect ray R(t)=p+t*d against AABB a. When intersecting, 
	 * return intersection distance tmin and point q of intersection
	 * 
	 * @param p
	 * @param d
	 * @param a
	 * @param tmin
	 * @param q
	 * @return
	 */
	private CollisionVariables intersectRayAABB( double[] p, double[] o, 
			Voxel a, CollisionVariables var)
	{
		var.setDistance(0.0);
		double[] d = Vector.minus(o, p);
		/* set to max distance ray can travel (for segment) */
		double tmax = Vector.distanceEuclid(p, o); 
		/* set to -FLT_MAX to get first hit on line */
		var.t = 0.0;  
		/* For all three slabs */
		for(int i=0; i<d.length; i++) 
		{ 
			/** TODO periodic */
			if ( Math.abs(d[i]) < EPSILON) 
			{ 
				/* Ray is parallel to slab. No hit if origin not within slab */
				if (p[i] < a.getLower()[i] || p[i] > a.getHigher()[i])
				{
					var.setDistance(Double.MAX_VALUE);
					return var;
				}
			} 
			else
			{ 
				/* Compute intersection t value of ray with near and far plane
				 * of slab */
				double ood = 1.0 / d[i]; 
				double t1 = ((a.getLower()[i] - p[i]) * ood); 
				double t2 = ((a.getHigher()[i] - p[i]) * ood); 
				/* Make t1 be intersection with near plane, t2 with far plane */
				if (t1 > t2) 
					Swap(t1, t2); 
				/* Compute the intersection of slab intersection intervals */
				if (t1 > var.t) 
					var.t = t1; 
				if (t2 > tmax) 
					tmax = t2; 
				/* Exit with no collision as soon as slab intersection becomes 
				 * empty. */
				if (var.t > tmax) 
				{
					var.setDistance(Double.MAX_VALUE);
					return var;
				}
			} 
		}
		var.interactionVector = Vector.add( p,Vector.times( d, var.t ) );
		return var;
	}
	
	/**
	 * TODO Real-time collision detection pp 197
	 * slight adjust of IntersectSegmentCylinder
	 * @return
	 */
	private boolean intersectSegmentCapsule(double[] sa, double[] sb, 
			double[] p, double[] q, double r, CollisionVariables var)
	{
		/* checking caps */
		var = linesegPoint( sa, sb, p, var );
		if(var.getDistance() < r + var.margin)
			return true;
		var = linesegPoint( sa, sb, q, var );
		if(var.getDistance() < r + var.margin)
			return true;
		/* checking cylinder */
		return intersectSegmentCylinder( sa, sb, p, q, r, var );
	}
	
	/**
	 * TODO Real-time collision detection pp 197
	 * IntersectSegmentCylinder
	 * @return
	 */
	private boolean intersectSegmentCylinder(double[] sa, double[] sb, 
			double[] p, double[] q, double r, CollisionVariables var)
	{
		double[] d =  Vector.minus(q, p);
		double[] m =  Vector.minus(sa, p);
		double[] n =  Vector.minus(sb, sa);
		double md = Vector.dotProduct(m, d); 
		double nd = Vector.dotProduct(n, d); 
		double dd = Vector.dotProduct(d, d); 

		/* Test if segment fully outside either endcap of cylinder */
		if (md < 0.0 && md + nd < 0.0) 
			return false; 
		/* Segment outside ’p’ side of cylinder */
		if (md > dd && md + nd > dd) 
			return false; 
		/* Segment outside ’q’ side of cylinder */
		double nn = Vector.dotProduct(n, n); 
		double mn = Vector.dotProduct(m, n); 
		double a = dd * nn - nd * nd;
		double k = Vector.dotProduct(m, m) - r*r; 
		double c = (dd * k - md * md); 
		if ( Math.abs(a) < EPSILON) { 
		/* Segment runs parallel to cylinder axis */
			if (c > 0.0) 
				return false;
		/* ’a’ and thus the segment lie outside cylinder. Now known that segment 
		 * intersects cylinder; figure out how it intersects. */
			if (md < 0.0)
				var.t =  (-mn/nn);
		/* Intersect segment against ’p’ endcap */
		else if (md > dd)
			var.t = (nd-mn) / nn; 
			/* Intersect segment against ’q’ endcap */
		else 
			var.t = 0.0;
		/* ’a’ lies inside cylinder */
		return true;
		}
		
		double b = (dd*mn-nd*md); 
		double discr = (b*b - a*c); 
		if (discr < 0.0) 
			return false;
		/* No real roots; no intersection */
		
		var.t = (-b - Math.sqrt(discr)) / a; 
		if (var.t < 0.0 || var.t > 1.0) 
			return false;
		/* Intersection lies outside segment */
		if( md+var.t*nd< 0.0) { 
			/* Intersection outside cylinder on ’p’ side */
			if (nd <= 0.0) 
				return false;
		/* Segment pointing away from endcap */
			var.t = -md / nd; 
		/* Keep intersection if Dot(S(t) - p, S(t) - p) <= r∧2 */
		return k+2*var.t*(mn+var.t*nn) <= 0.0;
		} 
		else if (md+var.t*nd>dd)
		{ 
			/* Intersection outside cylinder on ’q’ side */
			if (nd >= 0.0) 
				return false; 
			/* Segment pointing away from endcap */
			var.t = (dd - md) / nd; 
			/* Keep intersection if Dot(S(t) - q, S(t) - q) <= r∧2 */
			return k+dd-2*md+var.t*(2*(mn-nd)+var.t*nn) <= 0.0;
		}
		/* Segment intersects cylinder between the endcaps; t is correct */
		return true;
	}
	/* ***********************************************************************
	 * Helper methods
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
	
	/**
	 * \brief: Swap t1 and t2
	 * @param t1
	 * @param t2
	 */
	private void Swap(double t1, double t2) 
	{
		double temp = t1;
		t1 = t2;
		t2 = temp;
	}
	
	/**
	 * \brief: boolean interpretation of integer n
	 * @param n
	 * @return true if n != 0
	 */
	private boolean bool(int n)
	{
		return n != 0;
	}
	
	/**
	 * \brief: Support method that returns the AABB vertex with index n 
	 * @param b
	 * @param n
	 * @return
	 */
	private double[] Corner(Voxel b, int n) 
	{
		double[] p = new double[b.getHigher().length]; 
		for (int i = 0; i < p.length; i++)
			p[i] = (bool(n & 1) ? b.getHigher()[i] : b.getLower()[i]); 
		return p;
	}
}
