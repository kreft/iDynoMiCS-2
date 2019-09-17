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
public class AreNotColliding<A> implements Predicate<Collection<Surface>>
{
	private Collection<Surface> _surfaces;
	private Collision _collision;
	private double _margin = 0.0;
	
	public AreNotColliding(Surface surf, Collision collision)
	{
		this._surfaces = new LinkedList<Surface>();
		this._surfaces.add(surf);
		this._collision = collision;
	}
	
	public AreNotColliding(Surface surf, Collision collision, double margin)
	{
		this(surf, collision);
		this._margin = margin;
	}
	
	public AreNotColliding(Collection<Surface> surfaces, Collision collision)
	{
		this._surfaces = surfaces;
		this._collision = collision;
	}
	
	public AreNotColliding(Collection<Surface> surfaces, 
			Collision collision, double margin)
	{
		this(surfaces, collision);
		this._margin = margin;
	}
	
	@Override
	public boolean test(Collection<Surface> a) 
	{
		for ( Surface surf : this._surfaces )
			for ( Surface other : a )
				if ( this._collision.intersect(other, surf, this._margin) )
					return true;
		return false;
	}
}