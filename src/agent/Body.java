package agent;

import generalInterfaces.Duplicable;
import idynomics.NameRef;

import java.util.LinkedList;
import java.util.List;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import surface.*;

public class Body implements Duplicable {
	
	/**
	 * the body belongs to agent
	 * NOTE: this maybe taken out when we find an other solution for duplicables
	 */
	Agent agent; 
	
    /**
     * The 'body' of the agent is represented by sphere-swept volumes of a 
     * collection of points connected by springs of length lengths. This results
     * in a single sphere for coccoid-type agents or a tube for agents described
     * by multiple points.
     */
    protected List<Point> points = new LinkedList<Point>();
    
    /**
     * The surfaces describe the different segments of the agents body.
     */
    protected List<Surface> surfaces = new LinkedList<Surface>();

	/**
	 * Rest angles of torsion springs for multi-segment agents
	 */
	protected double[] _angles;
	
	/**
	 * NOTE: work in progress, subject to change
	 * Links with other agents/bodies owned by this body
	 * NOTE: this list does not contain links with this body owned by an other
	 * body
	 */
	public LinkedList<Link> _links = new LinkedList<Link>();
	
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
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
	 * NOTE: work in progress
	 * Hybrid: Coccoid, Rod, rods, TODO Chain
	 * @return
	 */
	public Body(List<Point> points, Agent agent)
	{
		this.points = points;
		if (points.size() == 1)
			this.surfaces.add( new Sphere(points.get(0), this) );
		if (points.size() == 2)
			this.surfaces.add( new Rod(new Point[]{ points.get(0), 
					points.get(1)} , this));
		if(points.size() > 2)
			Feedback.out(LogLevel.QUIET, "WARNING: assigning unsuported body "
					+ "type"); //TODO 
		this.agent = agent;
	}
	
	public Body(List<Point> points, double length, double radius, Agent agent)
	{
		this.agent = agent;
		this.points.addAll(points);
		if(this.points.size() == 1)
			this.surfaces.add(new Sphere(points.get(0), radius));
		else
		{
			for(int i = 0; points.size()-1 > i; i++)
			{
				this.surfaces.add(new Rod(points.get(i), points.get(i+1), 
						length, radius));
			}
		}
	}

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * 
	 * @return number of dimensions represented in the (first) point
	 */
	public int nDim() 
	{
		return points.get(0).nDim();
	}

	/**
	 * returns the radius of the body, obtained from the agent
	 * @return
	 */
	public double getRadius() {
		return (double) agent.get(NameRef.bodyRadius);
	}

	/**
	 * returns the spine length of the agent, obtained from the agent
	 * @return
	 */
	public double getLength() {
		return (double) agent.get(NameRef.bodyLength);
	}
	
	/**
	 * returns all points in the agent body
	 * @return
	 */
	public List<Point> getPoints()
	{
		return this.points;
	}
	
	//TODO: method will be replaced
	public List<double[]> getJoints()
	{
		List<double[]> joints = new LinkedList<double[]>();
		points.forEach( (p) -> joints.add(p.getPosition()) );
		return joints;
	}
	
	//FIXME only for testing purposes
	public Surface getSurface()
	{
		return this.surfaces.get(0);
	}
	
	/*************************************************************************
	 * general methods
	 ************************************************************************/
	
	/**
	 * returns a duplicate of this body and registers a new agent NOTE: does
	 * not set the agent's body state!
	 * TODO proper testing TODO make for multishape bodies
	 */
	public Body copy(Agent agent)
	{
		switch (surfaces.get(0).type())
		{
		case SPHERE:
			return new Body(new Sphere((Sphere) surfaces.get(0), this), agent);
		case ROD:
			return new Body(new Rod((Rod) surfaces.get(0), this), agent);
		default:
			return null;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	// TODO the bounding box related methods will be replaced by a bounding
	// box object
	/////////////////////////////////////////////////////////////////////////
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
}
