package agent.body;

import java.util.LinkedList;
import java.util.List;

public class Body
{
    /**
     * The 'body' of the agent is represented by sphere-swept volumes of a 
     * collection of points connected by springs of length lengths. This
     * results in a single sphere for coccoid-type agents or a tube for agents
     * described by multiple points.
     */
    protected List<Point> points 	= new LinkedList<Point>();
	
    /**
     * Rest length of internal springs connecting the points.
     */
	protected Double[] _lengths		= null;
	
	/**
	 * Rest angles of torsion springs.
	 */
	protected Double[] _angles		= null;
	
	/**
	 * Radius of the cell.
	 */
	protected Double _radius		= null;
	
	/**
	 * FIXME: convert to switch-case rather than if else
	 */
	public int getMorphologyIndex()
	{
		/*
		 * No body.
		 */
		if ( points.size() == 0 )
			return 0;
		/*
		 * Spherical body.
		 */
		else if ( points.size() == 1 )
			return 1;
		/*
		 * Rob-shaped body.
		 */
		else if ( points.size() == 2 )
			return 2;
		/*
		 * Bendable body/filament-type shape, depending on whether it has
		 * angles.
		 */
		else
			return ( _angles == null ) ? 3 : 4;
	}
}
