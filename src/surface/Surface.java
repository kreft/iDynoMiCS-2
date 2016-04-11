package surface;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Surface
{
	protected Collision collisionDomain;
	
	/**
	 * Unique identifier for each surface
	 */
    static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;
    
    /**
     * map of surfaces with which collisions need to be ignored
     * TODO implement
     */
    public HashMap<Integer, Surface> _collisionIgnored = new HashMap<Integer, Surface>();
    
	/**
	 * Used for boundaries, if a surface is bounding other objects are retained
	 * in the surface object (Sphere, rod, cylinder) rather than kept out.
	 */
	public boolean bounding;
	
    /**
     * Surface type
     *
     */
	public enum Type
	{
		SPHERE,
		ROD,
		PLANE,
		// INFINITECYLINDER
	}
	
	/**
	 * Sets the Collision class
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
	public abstract void set(double a, double b);
	
	/**
	 * returns the surface type
	 * @return
	 */
	public abstract Type type();
	
	/**
	 * returns the Collision class
	 * @return
	 */
	public Collision getCollisionDomain()
	{
		return collisionDomain;
	}
	
	/**
	 * returns the distance to another surface
	 * @param surface
	 * @return
	 */
	public double distanceTo(Surface surface)
	{
		return collisionDomain.distance(this, surface);
	}
	
	/**
	 * writes the resulting force from this collision to the force vector of
	 * the mass points of thes two involved surface objects
	 * @param surface
	 */
	public void collisionWith(Surface surface)
	{
		collisionDomain.distance(this, surface);
	}

}
