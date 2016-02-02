package surface;

import java.util.HashMap;

public abstract class Surface
{
	protected Collision collisionDomain;
	
    static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;
    
    public HashMap<Integer, Surface> _collisionIgnored = new HashMap<Integer, Surface>();
		
	public enum Type
	{
		SPHERE,
		ROD,
		PLANE,
		// INFINITECYLINDER
	}
	
	public boolean bounding;
	
	public void init(Collision collisionDomain) 
	{
		this.collisionDomain = collisionDomain;
	}
	
	public abstract Type type();
	
	public double distanceTo(Surface surface)
	{
		return collisionDomain.distance(this, surface);
	}
	
	public void collisionWith(Surface surface)
	{
		collisionDomain.distance(this, surface);
	}

}
