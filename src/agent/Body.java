package agent;

import generalInterfaces.Copyable;
import generalInterfaces.XMLable;
import generalInterfaces.HasBoundingBox;
import linearAlgebra.Vector;
import nodeFactory.ModelNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.XmlLabel;
import surface.*;

/**
 * \brief The 'body' of an agent is represented by sphere-swept volumes of a 
 * collection of points connected by springs of length lengths. This results
 * in a single sphere for coccoid-type agents or a tube for agents described
 * by multiple points.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Body implements Copyable, XMLable
{
	/**
	 * Ordered list of the points that describe the body.
	 */
	protected List<Point> _points = new LinkedList<Point>();

	/**
	 * The surfaces describe the different segments of the agents body.
	 */
	protected List<Surface> _surfaces = new LinkedList<Surface>();

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
	public LinkedList<Link> links = new LinkedList<Link>();


	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	/**
	 * Coccoid
	 */
	public Body(Point point, double radius)
	{
		this._points.add(point);
		this._surfaces.add(new Ball(point, radius));
	}

	public Body(Ball sphere)
	{
		this._points.add(sphere._point);
		this._surfaces.add(sphere);
	}
	
	public Body(double[] position, double radius)
	{
		this(new Point(position), radius);
	}

	/**
	 * Rod
	 * @param rod
	 */
	public Body(Point[] points, double spineLength, double radius)
	{
		this._points.add(points[0]);
		this._points.add(points[1]);
		this._surfaces.add(new Rod(points, spineLength, radius));
	}

	public Body(Rod rod)
	{
		this._points.add(rod._points[0]);
		this._points.add(rod._points[1]);
		this._surfaces.add(rod);
	}

	/**
	 * NOTE: work in progress
	 * Hybrid: Coccoid, Rod, rods, TODO Chain
	 * @return
	 */
	public Body(List<Point> points, double length, double radius)
	{

		this._points.addAll(points);
		if(this._points.size() == 1)
			this._surfaces.add(new Ball(points.get(0), radius));
		else
		{
			for(int i = 0; points.size()-1 > i; i++)
			{
				this._surfaces.add(new Rod(points.get(i), points.get(i+1), 
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
		NodeList pointNodes = s.getElementsByTagName(XmlLabel.point);
		for (int k = 0; k < pointNodes.getLength(); k++) 
		{
			Element point = (Element) pointNodes.item(k);
			pointList.add(new Point(Vector.dblFromString(
					point.getAttribute(XmlLabel.position))));
		}
		return new Body(pointList);
		// Bas [01.02.16] TODO: currently only agents can have a
		// body, look into this if other things alos need to be
		// able to have a body
	}

	/**
	 * quick solution to create body from string, currently only coccoid
	 * @param input
	 * @return
	 */
	public static Object getNewInstance(String input)
	{
		List<Point> pointList = new LinkedList<Point>();
		pointList.add(new Point(Vector.dblFromString(input)));
		return new Body(pointList);
	}
	
	// TODO required from xmlable interface
	public ModelNode getNode()
	{
		return null;
	}

	public void init(Element xmlElem)
	{
		//FIXME quick fix: copy/pasted from
		//"public static Body getNewInstance(Node xmlNode)"
		//FIXME: not finished only accounts for simple coccoids
		NodeList pointNodes = xmlElem.getElementsByTagName(XmlLabel.point);
		for (int k = 0; k < pointNodes.getLength(); k++) 
		{
			Element point = (Element) pointNodes.item(k);
			this._points.add(new Point(Vector.dblFromString(
					point.getAttribute(XmlLabel.position))));
		}

	}

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	/**
	 * @return Number of dimensions represented in the (first) point.
	 */
	public int nDim() 
	{
		return this._points.get(0).nDim();
	}

	/**
	 * @return All points in this body.
	 */
	public List<Point> getPoints()
	{
		return this._points;
	}

	public int getNumberOfPoints()
	{
		return this._points.size();
	}
	
	/**
	 * TODO if we want bodies with various spineLengths update..
	 * @param radius
	 * @param spineLength
	 */
	public void update(double radius, double spineLength)
	{
		for ( Surface s: this._surfaces )
			s.set(radius, spineLength);
	}

	//TODO: method will be replaced
	public List<double[]> getJoints()
	{
		List<double[]> joints = new LinkedList<double[]>();
		this._points.forEach( (p) -> joints.add(p.getPosition()) );
		return joints;
	}

	//FIXME only for testing purposes
	public Surface getSurface()
	{
		return this._surfaces.get(0);
	}

	public List<Surface> getSurfaces()
	{
		return this._surfaces;
	}

	public List<BoundingBox> getBoxes(double margin)
	{
		List<BoundingBox> boxes = new LinkedList<BoundingBox>();
		for ( Surface s : this._surfaces )
			boxes.add( ((HasBoundingBox) s).boundingBox(margin) );
		return boxes;
	}

	/*************************************************************************
	 * general methods
	 ************************************************************************/

	/**
	 * \brief Move a {@code Point} in this {@code Body} to the new location
	 * given, then move all other {@code Point}s so that the overall body shape
	 * stays the same.
	 * 
	 * @param newLocation
	 */
	public void relocate(double[] newLocation)
	{
		// TODO This can probably be done better using the links approach,
		// once this is completed.
		// TODO this won't currently work if newLocation has different length
		// to the number of dimensions the body has currently.
		// TODO the assumption that we use the first point is the simplest, but
		// probably not the best
		Point focus = this._points.get(0);
		HashMap<Point,double[]> relDiffs = new HashMap<Point,double[]>();
		double[] vector;
		for ( Point p : this._points )
		{
			vector = Vector.minus(focus.getPosition(), p.getPosition());
			relDiffs.put(p, vector);
		}
		focus.setPosition(newLocation);
		for ( Point p : this._points )
		{
			vector = relDiffs.get(p);
			Vector.addEquals(vector, p.getPosition());
			p.setPosition(vector);
		}
	}
	
	/**
	 * returns a copy of this body and registers a new agent NOTE: does
	 * not set the agent's body state!
	 * TODO proper testing TODO make for multishape bodies
	 */
	public Body copy()
	{
		switch ( this._surfaces.get(0).type() )
		{
		case SPHERE:
			return new Body(new Ball((Ball) this._surfaces.get(0)));
		case ROD:
			return new Body(new Rod((Rod) this._surfaces.get(0)));
		default:
			return null;
		}
	}

	@Override
	public String getXml() {
		// TODO Auto-generated method stub
		return null;
	}
}
