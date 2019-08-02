package agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import generalInterfaces.HasBoundingBox;
import instantiable.Instantiable;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;
import surface.*;
import utility.Helper;

/**
 * \brief The 'body' of an agent is represented by sphere-swept volumes of a 
 * collection of points connected by springs of length lengths. This results
 * in a single sphere for coccoid-type agents or a tube for agents described
 * by multiple points.
 * 
 * TODO amount of constructors gets quite big, consider to reduce and simplify
 * if possible.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Body implements Copyable, Instantiable, Settable 
{
	/*
	 * morphology specifications
	 */
	public enum Morphology {
		COCCOID,
		BACILLUS,
		CUBOID,
	}
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
	 * morphology
	 */
	protected Morphology _morphology;

	private Settable _parentNode;
	/**
	 * NOTE: work in progress, subject to change
	 * Links with other agents/bodies owned by this body
	 * NOTE: this list does not contain links with this body owned by an other
	 * body
	 */
	protected LinkedList<Link> _links = new LinkedList<Link>();


	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	public Body()
	{
		/* Instantiatable */
	}
	/**
	 * Coccoid
	 */
	public Body(Point point, double radius)
	{
		this._points.add(point);
		this._surfaces.add(new Ball(point, radius));
		this._morphology = Morphology.COCCOID;
	}

	public Body(Ball sphere)
	{
		this._points.add(sphere._point);
		this._surfaces.add(sphere);
		this._morphology = Morphology.COCCOID;
	}
	
	public Body(double[] position, double radius)
	{
		this(new Point(position), radius);
		this._morphology = Morphology.COCCOID;
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
		this._morphology = Morphology.BACILLUS;
	}

	public Body(Rod rod)
	{
		this._points.add(rod._points[0]);
		this._points.add(rod._points[1]);
		this._surfaces.add(rod);
		this._morphology = Morphology.BACILLUS;
	}
	
	public Body(Point[] points)
	{
		this._points.add(points[0]);
		this._points.add(points[1]);
		this._morphology = Morphology.CUBOID;
	}
		
	/**
	 * Minimal random body generation assuming length and radius = 0.0;
	 * @param morphology
	 * @param domain
	 */
	public Body(Morphology morphology, BoundingBox domain)
	{
		this(morphology, domain, 0.0, 0.0);
	}
	
	/**
	 * Full random body
	 * 
	 * @param morphology
	 * @param domain
	 * @param radius
	 * @param length
	 */
	public Body(Morphology morphology, BoundingBox domain, 
			double radius, double length)
	{
		this(morphology, domain.getRandomInside(),  radius, length);
	}
	
	/**
	 * Body with random second point
	 * 
	 * @param morphology
	 * @param domain
	 * @param radius
	 * @param length
	 */
	public Body(Morphology morphology, double[] position,  
			double radius, double length)
	{
		this(morphology, position, Vector.add( position, 
				Vector.randomZeroOne( position.length ) ),  radius, length);
	}

	/**
	 * body at position
	 * 
	 * @param morphology
	 * @param domain
	 * @param radius
	 * @param length
	 */
	public Body(Morphology morphology, double[] positionA, double[] positionB, 
			double radius, double length)
	{
		switch (morphology)
		{
			case COCCOID :
				this._points.add( new Point( positionA ) );
				this._surfaces.add( new Ball(this._points.get(0), radius) );
				this._morphology = Morphology.COCCOID;
				break;
			case BACILLUS :
				Point p = new Point( positionA );
				this._points.add( p );
				this._points.add( new Point( positionB ) );
				this._surfaces.add(new Rod(_points.get(0), _points.get(1), 
						radius, length));
				this._morphology = Morphology.BACILLUS;
				break;
			case CUBOID :
				/* TODO */
				if (Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, Morphology.CUBOID.name() + " random "
							+ "body generation not suported, skipping..");
			default: 
				break;
		}
	}
	
	/**
	 * NOTE: work in progress
	 * Hybrid: Coccoid, Rod, rods, TODO Chain
	 * @return
	 */
	public Body(List<Point> points, double length, double radius)
	{
		// This enables the program to distinguish
		// rods and balls depending on number of points.

		this._points.addAll(points);
		this.assignMorphology(null);
		this.constructBody(length, radius);
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
	
	public void assignMorphology(String morphology)
	{
		if (morphology != null)
			this._morphology = Morphology.valueOf(morphology);
		else if(this._points.size() == 1)
			this._morphology = Morphology.COCCOID;
		else
			this._morphology = Morphology.BACILLUS;
	}
	
	public void constructBody()
	{
		constructBody( 0.0, 0.0 );
	}
	
	public void constructBody( double length, double radius )
	{
		switch (this._morphology)
		{
		case COCCOID :
			this._surfaces.add(new Ball(_points.get(0), radius));
			break;
		case BACILLUS :
			for(int i = 0; _points.size()-1 > i; i++)
			{
				this._surfaces.add(new Rod(_points.get(i), 
						_points.get(i+1), length, radius)); 
			}
			break;
		case CUBOID :
			/* TODO */
			if (Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, Morphology.CUBOID.name()
						+ "body generation not suported yet, skipping..");
		default: 
			break;
		}

	}
	/**
	 * quick solution to create body from string
	 * @param input
	 * @return
	 */
	public static Object instanceFromString(String input)
	{
		if ( Helper.isNullOrEmpty(input) )
			input = Helper.obtainInput(input, "position vector", true);
		
		List<Point> pointList = new LinkedList<Point>();
		String[] points = input.split(Matrix.DELIMITER);
		for (String s : points)
			pointList.add(new Point(Vector.dblFromString(s)));
		
		return new Body(pointList);
	}

	public void instantiate(Element xmlElem, Settable parent)
	{
		if( !Helper.isNullOrEmpty( xmlElem ))
		{
			/* obtain all body points */
			List<Point> pointList = new LinkedList<Point>();
			Collection<Element> pointNodes =
			XmlHandler.getAllSubChild(xmlElem, XmlRef.point);
			for (Element e : pointNodes) 
			{
				Element point = e;
				pointList.add(new Point(Vector.dblFromString(
						point.getAttribute(XmlRef.position))));
			}
			this._points.addAll(pointList);
			
			/* assign a body morphology */
			String morphology = 
					XmlHandler.gatherAttribute(xmlElem, XmlRef.morphology);
			if (morphology == null )
				morphology = XmlHandler.gatherAttributeFromUniqueNode(xmlElem, 
						XmlRef.agentBody, XmlRef.morphology);
			assignMorphology(morphology);
			
			constructBody();
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
		{
			if (s instanceof Rod)
			{
				((Rod) s).setLength(spineLength);
				((Rod) s).setRadius(radius);
			}
			else if (s instanceof Ball)
			{
				((Ball) s).setRadius(radius);
			}
		}
	}
	
	/**
	 * Returns the position vector of a given joint. Ball has only one joint
	 * (the first point) rods have 2 joints etc.
	 * @param joint numbered starting with 0
	 * @return position vector
	 */
	public double[] getPosition(int joint)
	{
		return this._points.get(joint).getPosition();
	}
	
	public List<Surface> getSurfaces()
	{
		return this._surfaces;
	}

	public List<BoundingBox> getBoxes(double margin, Shape shape)
	{
		List<BoundingBox> boxes = new LinkedList<BoundingBox>();
		for ( Surface s : this._surfaces )
			boxes.add( ((HasBoundingBox) s).boundingBox(margin, shape) );
		return boxes;
	}
	
	public double[] getCenter()
	{
		if (this.getNumberOfPoints() == 1)
			return this._points.get(0).getPosition();
		double[] center = Vector.vector(this.nDim(),0.0);
		for ( Point p : this.getPoints() )
		{
			Vector.addEquals(center, p.getPosition());
		}
		return Vector.divideEqualsA(center, (double) this.getNumberOfPoints());
	}

	public Morphology getMorphology() 
	{
		return this._morphology;
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
	public Module getModule() {
		Module modelNode = new Module(XmlRef.agentBody, this);
		modelNode.setRequirements(Requirements.ZERO_OR_ONE);
		
		modelNode.add(new Attribute(XmlRef.morphology, 
				this._morphology.toString(), null, false ));

		for (Point p : this.getPoints() )
			modelNode.add(p.getModule() );
		
		return modelNode;
	}

	@Override
	public String defaultXmlTag()
	{
		return XmlRef.agentBody;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}

}
