package agent.body;

import java.util.LinkedList;
import java.util.List;

import agent.Agent.StatePredicate;

public class Body {
    
	/**
	 * 
	 */
	public final static StatePredicate<Object> tester = new StatePredicate<Object>()
	{
		public boolean test(Object aState)
		{
			return aState instanceof Body;
		}
	};
	
    /**
     * The 'body' of the agent is represented by sphere-swept volumes of a 
     * collection of points connected by springs of length lengths. This results
     * in a single sphere for coccoid-type agents or a tube for agents described
     * by multiple points.
     */
    protected List<Point> points 	= new LinkedList<Point>();
	
    /**
     * Rest length of internal springs connecting the points.
     */
	protected Double[] _lengths		= null;
	
	/**
	 * Rest angles of torsion springs 
	 */
	protected Double[] _angles		= null;
	
	/**
	 * radius of the cell
	 */
	protected Double _radius		= null;
	
	/**
	 * FIXME: convert to switch-case rather than if else
	 */
	public int getMorphologyIndex()
	{
		if (points.size() == 0)
			return 0;					// no body
		else if (points.size() == 1)
			return 1;					// cocoid body
		else if (points.size() == 2)
			return 2;					// rod body
		else if (points.size() > 2)
		{
			if (_angles == null)
				return 3;				// bendable body / filaments
			else
				return 4;				// bend body type
		}
		else
			return -1;					// undefined body type
		
	}
	
	public float[] coord(Double radius) 
	{
		if(points.size() == 1)
			return points.get(0).coord(radius);
		float[] coord = new float[nDim()];
		for (Point o: points) 
		{
			for (int i = 0; i < nDim(); i++) 
			{
				coord[i] = Math.min(coord[i], o.coord(radius)[i]);
			}
		}
		return coord;
	}
	
	public float[] coord(Double radius, double t) 
	{
		if(points.size() == 1)
			return points.get(0).coord(radius);
		float[] coord = new float[nDim()];
		for (Point o: points) 
		{
			for (int i = 0; i < nDim(); i++) 
			{
				coord[i] = Math.min(coord[i], o.coord(radius)[i]) - (float) t;
			}
		}
		return coord;
	}
	
	public float[] upper(Double radius) 
	{
		float[] upper = new float[nDim()];
		for (Point o: points) 
		{
			for (int i = 0; i < nDim(); i++) 
			{
				upper[i] = Math.max(upper[i], o.upper(radius)[i]);
			}
		}
		return upper;
	}
	
	public float[] dimensions(Double radius) 
	{
		if(points.size() == 1)
			return points.get(0).dimensions(radius);
		float[] coord 		= coord(radius);
		float[] upper 		= upper(radius);
		float[] dimensions	= new float[nDim()];
		for (int i = 0; i < nDim(); i++)
			dimensions[i] = upper[i] - coord[i];
		return dimensions;
	}
	
	public float[] dimensions(Double radius, double t) 
	{
		if(points.size() == 1)
			return points.get(0).dimensions(radius);
		float[] coord 		= coord(radius);
		float[] upper 		= upper(radius);
		float[] dimensions	= new float[nDim()];
		for (int i = 0; i < nDim(); i++)
			dimensions[i] = upper[i] - coord[i] + 2 * (float) t;
		return dimensions;
	}
	
	public int nDim() 
	{
		return points.get(0).nDim();
	}
}
