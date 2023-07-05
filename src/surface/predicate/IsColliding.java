package surface.predicate;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Predicate;

import surface.Surface;
import surface.collision.Collision;

/**
 * \brief 
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * 
 * @param <A>
 */
public class IsColliding<A> implements Predicate<Surface>
{
	private Collection<Surface> _surfaces;
	private Collision _collision;
	private double _margin = 0.0;
	
	public IsColliding(Surface surf, Collision collision)
	{
		this._surfaces = new LinkedList<Surface>();
		this._surfaces.add(surf);
		this._collision = collision;
	}
	
	public IsColliding(Surface surf, Collision collision, double margin)
	{
		this(surf, collision);
		this._margin = margin;
	}
	
	public IsColliding(Collection<Surface> surfaces, Collision collision)
	{
		this._surfaces = surfaces;
		this._collision = collision;
	}
	
	public IsColliding(Collection<Surface> surfaces, 
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
				return true;
		return false;
	}
}