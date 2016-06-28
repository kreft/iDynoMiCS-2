package surface.predicate;

import java.util.function.Predicate;

import surface.Collision;
import surface.Surface;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <A>
 */
public class AreNotColliding<A> implements Predicate<Surface>
{
	private Surface _surf;
	private Collision _collision;
	private double _margin = 0.0;
	
	public AreNotColliding(Surface surf, Collision collision)
	{
		this._surf = surf;
		this._collision = collision;
	}
	
	public AreNotColliding(Surface surf, Collision collision, double margin)
	{
		this(surf, collision);
		this._margin = margin;
	}
		
	@Override
	public boolean test(Surface a) 
	{
		return ! this._collision.areColliding(a, this._surf, this._margin);
	}
}