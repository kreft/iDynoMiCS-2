package surface;

import java.util.HashMap;
import generalInterfaces.HasBoundingBox;
import settable.Module;
import shape.Shape;
import surface.collision.Collision;

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
		VOXEL,
		CUBOID,
		// CYLINDER NOTE for cylindrical domains
	}
	
	/**
	 * Unique identifier for each surface.
	 */
	private static int UNIQUE_ID = 0;
	protected int _uid = ++UNIQUE_ID;

	/**
	 * TODO
	 */
	protected Collision _collisionDomain;

	/**
	 * Map of surfaces with which collisions need to be ignored. Neighboring
	 * surfaces in the same body (like a bendable rod) would need to be ignored
	 * since otherwise the intentionally overlapping segments would repel each
	 * other.
	 */
	// TODO implement
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
		this._collisionDomain = collisionDomain;
	}
	

	public abstract Module appendToModule(Module modelNode);
	
	public abstract int dimensions();

	/**
	 * @return the surface type
	 */
	public abstract Type type();

	/**
	 * @return The Collision object for this Surface.
	 */
	public Collision getCollisionDomain()
	{
		return this._collisionDomain;
	}

	/**
	 * returns the distance to another surface
	 * @param surface
	 * @return
	 */
	public double distanceTo(Surface surface)
	{
		return this._collisionDomain.distance(this, surface);
	}

	/**
	 * \brief Writes the resulting force from this collision to the force vector of
	 * the mass points of the two involved surface objects
	 * 
	 * <p>This method always also sets the internal variables _flip and dP of 
	 * {@link #_collisionDomain}. It may also set s and t, depending on the
	 * surface types.</p>
	 * 
	 * @param surface
	 */
	public boolean collisionWith(Surface surface)
	{
		return (this._collisionDomain.distance(this, surface) < 0.0);
	}

	/**
	 * return a bounding box with margin if applicable for surface
	 * @param margin
	 * @return
	 */
	public BoundingBox getBox(double margin, Shape shape)
	{
		if (this instanceof HasBoundingBox)
			return ((HasBoundingBox) this).boundingBox(margin, shape) ;
		return null;
	}
}
