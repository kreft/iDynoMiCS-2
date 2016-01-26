package surface;

public abstract class Surface
{
	protected Collision collisionDomain;
		
	public enum Type
	{
		SPHERE,
		ROD,
		STRAND, // for filaments, needs to be implemented still
		PLANE,
	}
	
	public void init(Collision collisionDomain) 
	{
		this.collisionDomain = collisionDomain;
	}
	
	public abstract Type type();

}
