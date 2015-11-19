package agent.body;

import java.util.LinkedList;
import java.util.List;

public class Body {
	
	/**
	 * 
	 */
	public Body(List<Point> points, Double[] lengths, Double[] angles, Double radius) 
	{
		this.points = points;
		this._lengths = lengths;
		this._angles = angles;
		this._radius = radius;		
	}
	
	public Body(List<Point> points) 
	{
		this.points = points;
		this._lengths = null;
		this._angles = null;
		this._radius = null;		
	}
	
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
	 * radius of the cell (not used for coccoid cell types)
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
	
	public List<Double[]> getJoints()
	{
		List<Double[]> joints = new LinkedList<Double[]>();
		for (int i = 0; points.size() > i; i++)
		{
			joints.add(points.get(i).getPosition());
		}
		return joints;
	}
	
	/**
	 * 
	 * @param radius
	 * @return coordinates of lower corner of bounding box
	 */
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
	
	/**
	 * 
	 * @param radius
	 * @param t: added margin
	 * @return coordinates of lower corner of bounding box with margin
	 */
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
	
	/**
	 * 
	 * @param radius
	 * @return coordinates of upper corner of bounding box
	 */
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
	
	/**
	 * 
	 * @param radius
	 * @return dimensions of the bounding box
	 */
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
	
	/**
	 * 
	 * @param radius
	 * @param t: margin
	 * @return dimensions of the bounding box with added margin
	 */
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
	
	/**
	 * 
	 * @return number of dimensions represented in the (first) point
	 */
	public int nDim() 
	{
		return points.get(0).nDim();
	}
}
