package agent.body;

import generalInterfaces.Copyable;

import java.util.LinkedList;
import java.util.List;

import linearAlgebra.Vector;

public class Body implements Copyable {
	
	/**
	 * 
	 */
	public Body(List<Point> points, double[] lengths, double[] angles, double radius) 
	{
		this.points = points;
		this._lengths = lengths;
		this._angles = angles;
		this._radius = radius;		
	}
	
	public Body(List<Point> points) 
	{
		this.points = points;
		/*
		 * Lengths, angles and radius remain undefined.
		 */
	}
	
	public Body copy()
	{
		//TODO make this
		List<Point> newPoints = new LinkedList<Point>();
		for ( Point p : points)
		{
			Point duplicate = new Point(Vector.copy(p.getPosition()));
			newPoints.add(duplicate);
		}
			
		return new Body(newPoints);
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
	protected double[] _lengths;
	
	/**
	 * Rest angles of torsion springs 
	 */
	protected double[] _angles;
	
	/**
	 * radius of the cell (not used for coccoid cell types)
	 * FIXME: this is confusing, there should be a better way of doing this
	 */
	protected double _radius;
	
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
	
	public List<double[]> getJoints()
	{
		List<double[]> joints = new LinkedList<double[]>();
		points.forEach( (p) -> joints.add(p.getPosition()) );
		return joints;
	}
	
	public List<Point> getPoints()
	{
		return this.points;
	}
	
	/**
	 * 
	 * @param radius
	 * @return coordinates of lower corner of bounding box
	 */
	public double[] coord(double radius) 
	{
		if ( points.size() == 1 )
			return points.get(0).coord(radius);
		double[] coord = new double[nDim()];
		for (Point o: points) 
			for ( int i = 0; i < nDim(); i++ ) 
				coord[i] = Math.min( coord[i], o.coord(radius)[i] );
		return coord;
	}
	
	/**
	 * 
	 * @param radius
	 * @param t: added margin
	 * @return coordinates of lower corner of bounding box with margin
	 */
	public double[] coord(double radius, double t) 
	{
		if ( points.size() == 1 )
			return points.get(0).coord(radius);
		double[] coord = new double[nDim()];
		for (Point o: points) 
			for ( int i = 0; i < nDim(); i++ ) 
				coord[i] = Math.min(coord[i], o.coord(radius)[i]) - t;
		return coord;
	}
	
	/**
	 * 
	 * @param radius
	 * @return coordinates of upper corner of bounding box
	 */
	public double[] upper(Double radius) 
	{
		double[] upper = new double[nDim()];
		for (Point o: points) 
			for ( int i = 0; i < nDim(); i++ ) 
				upper[i] = Math.max( upper[i], o.upper(radius)[i] );
		return upper;
	}
	
	/**
	 * 
	 * @param radius
	 * @return dimensions of the bounding box
	 */
	public double[] dimensions(Double radius) 
	{
		if(points.size() == 1)
			return points.get(0).dimensions(radius);
		double[] coord 		= coord(radius);
		double[] upper 		= upper(radius);
		double[] dimensions	= new double[nDim()];
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
	public double[] dimensions(Double radius, double t) 
	{
		if(points.size() == 1)
			return points.get(0).dimensions(radius);
		double[] coord 		= coord(radius);
		double[] upper 		= upper(radius);
		double[] dimensions	= new double[nDim()];
		for (int i = 0; i < nDim(); i++)
			dimensions[i] = upper[i] - coord[i] + 2 * t;
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
