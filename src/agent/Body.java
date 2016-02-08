package agent;

import generalInterfaces.Copyable;
import generalInterfaces.XMLable;
import linearAlgebra.Vector;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import surface.*;

public class Body implements Copyable, XMLable {
	
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
	public Body(Point point, double radius)
	{
		this.points.add(point);
		this.surfaces.add(new Ball(point, radius));
	}
	
	public Body(Ball sphere)
	{
		this.points.add(sphere._point);
		this.surfaces.add(sphere);
	}
	
	/**
	 * Rod
	 * @param rod
	 */
	public Body(Point[] points, double spineLength, double radius)
	{
		this.points.add(points[0]);
		this.points.add(points[1]);
		this.surfaces.add(new Rod(points, spineLength, radius));
	}
	
	public Body(Rod rod)
	{
		this.points.add(rod._points[0]);
		this.points.add(rod._points[1]);
		this.surfaces.add(rod);
	}
	
	/**
	 * NOTE: work in progress
	 * Hybrid: Coccoid, Rod, rods, TODO Chain
	 * @return
	 */
	public Body(List<Point> points, double length, double radius)
	{

		this.points.addAll(points);
		if(this.points.size() == 1)
			this.surfaces.add(new Ball(points.get(0), radius));
		else
		{
			for(int i = 0; points.size()-1 > i; i++)
			{
				this.surfaces.add(new Rod(points.get(i), points.get(i+1), 
						length, radius));
			}
		}
	}
	
	/**
	 * this method can only be used if the body.update(radius, length) method
	 * is called before the body is used.
	 * @param points
	 */
	public Body(List<Point> points)
	{
		this(points, 0.0, 0.0);
	}
	
	/**
	 * First implementation of xmlable, not finished but functional
	 * @param xmlNode
	 * @return
	 */
	public static Body getNewInstance(Node xmlNode)
	{
		Element s = (Element) xmlNode;
		//FIXME: not finished only accounts for simple coccoids
		List<Point> pointList = new LinkedList<Point>();
		NodeList pointNodes = s.getElementsByTagName("point");
		for (int k = 0; k < pointNodes.getLength(); k++) 
		{
			Element point = (Element) pointNodes.item(k);
			pointList.add(new Point(Vector.dblFromString(
					point.getAttribute("position"))));
		}
		return new Body(pointList);
		// Bas [01.02.16] TODO: currently only agents can have a
		// body, look into this if other things alos need to be
		// able to have a body
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
	 * returns all points in the agent body
	 * @return
	 */
	public List<Point> getPoints()
	{
		return this.points;
	}
	
	
	/**
	 * TODO if we want bodies with various spineLengths update..
	 * @param radius
	 * @param spineLength
	 */
	public void update(double radius, double spineLength)
	{
		for(Surface s: surfaces)
			s.set(radius, spineLength);
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
	 * returns a copy of this body and registers a new agent NOTE: does
	 * not set the agent's body state!
	 * TODO proper testing TODO make for multishape bodies
	 */
	public Body copy()
	{
		switch (surfaces.get(0).type())
		{
		case SPHERE:
			return new Body(new Ball((Ball) surfaces.get(0)));
		case ROD:
			return new Body(new Rod((Rod) surfaces.get(0)));
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
