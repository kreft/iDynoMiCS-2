package surface.predicate;

import java.util.function.Predicate;

import surface.Collision;
import surface.Surface;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <a>
 * @param <b>
 */
public class Colliding<a> implements Predicate<Surface>
{
	private Surface _surf;
	private Collision _collision;
	private double _margin = 0.0;
	
	public Colliding(Surface surf, Collision collision)
	{
		this._surf = surf;
		this._collision = collision;
	}
	
	public Colliding(Surface surf, Collision collision, double margin)
	{
		this(surf, collision);
		this._margin = margin;
	}
		
	@Override
	public boolean test(Surface a) 
	{
		return _collision.colliding(a, _surf, _margin);
	}
}