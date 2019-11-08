package surface.predicate;

import java.util.Collection;
import java.util.function.Predicate;

import surface.Surface;
import surface.collision.Collision;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * 
 * @param <A>
 */
public class IsNotColliding<A> implements Predicate<Surface>
{
	private Collection<Surface> _surfaces;
	private Collision _collision;
	private double _margin = 0.0;
	
	public IsNotColliding(Collection<Surface> surfaces, Collision collision)
	{
		this._surfaces = surfaces;
		this._collision = collision;
	}
	
	public IsNotColliding(Collection<Surface> surfaces, 
			Collision collision, double margin)
	{
		this(surfaces, collision);
		this._margin = margin;
	}
	
	@Override
	public boolean test(Surface a) 
	{
		for ( Surface surf : this._surfaces )
			if ( this._collision.intersect(a, surf, this._margin) )
				return false;
		return true;
	}
}