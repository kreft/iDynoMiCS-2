package agent.body;

import generalInterfaces.Copyable;
import idynomics.NameRef;

import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import surface.*;

public class Body implements Copyable {
	
	/**
	 * the body belongs to agent
	 */
	Agent agent; 
	
	/**
	 * TODO: Depricated, remove
	 */
	public Body(List<Point> points, double[] lengths, double[] angles, double radius) 
	{
		this.points = points;
		this._angles = angles;		
	}
	
	public Body(List<Point> points) 
	{
		this.points = points;
		/*
		 * Lengths, angles and radius remain undefined.
		 */
	}
	
	
	// TODO some proper testing
	/**
	 * Coccoid
	 */
	public Body(Point point, Agent agent)
	{
		this.points.add(point);
		this.surfaces.add(new Sphere(point, this));
	}
	
	public Body(Sphere sphere, Agent agent)
	{
		this.points.add(sphere._point);
		sphere.setBody(this);
		this.surfaces.add(sphere);
		this.agent = agent;
	}
	
	/**
	 * Rod
	 * @param rod
	 */
	public Body(Point[] points, Agent agent)
	{
		this.points.add(points[0]);
		this.points.add(points[1]);
		this.surfaces.add(new Rod(points, this));
		this.agent = agent;
	}
	
	public Body(Rod rod, Agent agent)
	{
		this.points.add(rod._points[0]);
		this.points.add(rod._points[1]);
		rod.setBody(this);
		this.surfaces.add(rod);
		this.agent = agent;
	}

	
	/**
	 * Hybrid: Coccoid, Rod, Chain of rods
	 * @return
	 */
	public Body(List<Point> points, double length, double radius)
	{
		this.points.addAll(points);
		if(this.points.size() == 1)
			this.surfaces.add(new Sphere(points.get(0), radius));
		else
		{
			for(int i = 0; points.size()-1 > i; i++)
			{
				this.surfaces.add(new Rod(points.get(i), points.get(i+1), length, radius));
			}
		}
	}

	//TODO proper testing
	public Body copy()
	{
		// TODO make for multishape bodies
		switch (surfaces.get(0).type())
		{
		case SPHERE:
			return new Body(new Sphere((Sphere) surfaces.get(0)), this.agent);
		case ROD:
			return new Body(new Rod((Rod) surfaces.get(0)), this.agent);
		default:
			return null;
		}
		
	}
	
    /**
     * The 'body' of the agent is represented by sphere-swept volumes of a 
     * collection of points connected by springs of length lengths. This results
     * in a single sphere for coccoid-type agents or a tube for agents described
     * by multiple points.
     */
    protected List<Point> points = new LinkedList<Point>();
    
    protected List<Surface> surfaces = new LinkedList<Surface>();

	/**
	 * Rest angles of torsion springs 
	 */
	protected double[] _angles;
	
	//TODO: take out
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

	// obtains the radius which can be a primary or secondary agent state
	public double getRadius() {
		return (double) agent.get(NameRef.bodyRadius);
	}

	public double getLength() {
		return (double) agent.get(NameRef.bodyLength);
	}
}
