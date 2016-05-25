package surface;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Surface
{
	/**
	 * Surface type.
	 */
	// NOTE Rob [17/5/2016]: I think this may be a rare case where instanceof 
	// is an appropriate choice, and so this enum may be unnecessary.
	public enum Type
	{
		SPHERE,
		ROD,
		PLANE,
		// INFINITECYLINDER
	}
	
	/**
	 * Unique identifier for each surface.
	 */
	private static int UNIQUE_ID = 0;
	protected int _uid = ++UNIQUE_ID;

	/**
	 * TODO
	 */
	protected Collision collisionDomain;

	/**
	 * Map of surfaces with which collisions need to be ignored.
	 */
	// TODO implement
	// TODO Rob [17/5/2016]: Please give some explanation of the purpose.
	public HashMap<Integer, Surface> _collisionIgnored = new HashMap<Integer, Surface>();

	/**
	 * Used for boundaries, if a surface is bounding other objects are retained
	 * in the surface object (Sphere, rod, cylinder) rather than kept out.
	 */
	public boolean bounding;

	/**
	 * \brief Set the Collision class.
	 * 
	 * @param collisionDomain
	 */
	public void init(Collision collisionDomain) 
	{
		this.collisionDomain = collisionDomain;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 */
	// FIXME Rob [17/5/2016]: This is a very vague method, consider replacing.
	public abstract void set(double a, double b);

	/**
	 * @return the surface type
	 */
	public abstract Type type();

	/**
	 * @return The Collision object for this Surface.
	 */
	public Collision getCollisionDomain()
	{
		return this.collisionDomain;
	}

	/**
	 * returns the distance to another surface
	 * @param surface
	 * @return
	 */
	public double distanceTo(Surface surface)
	{
		return this.collisionDomain.distance(this, surface);
	}

	/**
	 * writes the resulting force from this collision to the force vector of
	 * the mass points of thes two involved surface objects
	 * @param surface
	 */
	public void collisionWith(Surface surface)
	{
		this.collisionDomain.distance(this, surface);
	}

}
