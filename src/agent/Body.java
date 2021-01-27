package agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import generalInterfaces.HasBoundingBox;
import instantiable.Instance;
import instantiable.Instantiable;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import shape.Shape;
import surface.Ball;
import surface.BoundingBox;
import surface.Point;
import surface.Rod;
import surface.Surface;
import surface.link.LinearSpring;
import surface.link.Link;
import surface.link.Spring;
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
	protected List<Point> _points;

	/**
	 * The surfaces describe the different segments of the agents body.
	 */
	protected List<Surface> _surfaces;
	
	protected Spring spine;
	
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
	 * 
	 * This list might be slower but thread safe, check it
	 */
	protected List<Link> _links = new CopyOnWriteArrayList<Link>();


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
		this._points = new LinkedList<Point>();
		this._points.add(point);
		this._surfaces = new LinkedList<Surface>();
		this._surfaces.add(new Ball(point, radius));
		this._morphology = Morphology.COCCOID;
	}

	public Body(Ball sphere)
	{
		this._points = new LinkedList<Point>();
		this._points.add(sphere._point);
		this._surfaces = new LinkedList<Surface>();
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
		this._points = new LinkedList<Point>();
		this._points.add(points[0]);
		this._points.add(points[1]);
		this._surfaces = new LinkedList<Surface>();
		this._surfaces.add(new Rod(points, spineLength, radius));
		this._morphology = Morphology.BACILLUS;
	}

	public Body(Rod rod)
	{
		this._points = new LinkedList<Point>();
		this._points.add(rod._points[0]);
		this._points.add(rod._points[1]);
		this._surfaces = new LinkedList<Surface>();
		this._surfaces.add(rod);
		this._morphology = Morphology.BACILLUS;
	}
	
	public Body(Point[] points)
	{
		this._points = new LinkedList<Point>();
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
		this._surfaces = new LinkedList<Surface>();
		switch (morphology)
		{
			case COCCOID :
				this._points = new LinkedList<Point>();
				this._points.add( new Point( positionA ) );
				this._surfaces.add( new Ball(this._points.get(0), radius) );
				this._morphology = Morphology.COCCOID;
				break;
			case BACILLUS :
				this._points = new LinkedList<Point>();
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
		this._points = new LinkedList<Point>();
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
		this._surfaces = new LinkedList<Surface>();
		switch (this._morphology)
		{
		case COCCOID :
			this._surfaces.add(new Ball(_points.get(0), radius));
			this.spine = null;
			break;
		case BACILLUS :
			for(int i = 0; _points.size()-1 > i; i++)
			{
				Rod out = new Rod(_points.get(i), 
						_points.get(i+1), length, radius);
				this._surfaces.add(out); 
				this.spine = new LinearSpring(1e6, out._points , 
						null, length);
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
			this._points = pointList;
			Collection<Element> linkers =
			XmlHandler.getAllSubChild(xmlElem, XmlRef.link);
			for (Element e : linkers) 
			{
				Link l = (Link) Instance.getNew(e, this, ClassRef.link);
				this._links.add(l);
			}
			
			
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

	public void setPoints(List<Point> points)
	{
		for (int i = 0; i < points.size(); i++)
		{
			this._points.get(i).setPosition(points.get(i).getPosition());
		}
		
	}
	
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

	public List<BoundingBox> getBoxes(double margin, Shape shape)
	{
		List<BoundingBox> boxes = new LinkedList<BoundingBox>();
		for ( Surface s : this._surfaces )
			boxes.add( ((HasBoundingBox) s).boundingBox(margin, shape) );
		return boxes;
	}
	
	public double[] getCenter(Shape shape)
	{
		if (this.getNumberOfPoints() == 1)
			return this._points.get(0).getPosition();
		double[] center = Vector.vector(this.nDim(),0.0);
		for ( Point p : this.getPoints() )
		{
			Vector.addEquals(center, shape.getNearestShadowPoint(p.getPosition()
					, this._points.get(0).getPosition()));
		}
		center = Vector.divideEqualsA(center, (double)this.getNumberOfPoints());
		return shape.getVerifiedLocation(center);
	}
	
	public Point getClosePoint(double[] location, Shape shape)
	{
		double old = Double.MAX_VALUE;
		Point hold = null;
		for( Point p : this._points)
		{
			double t = Vector.distanceEuclid( shape.getNearestShadowPoint(
					p.getPosition(), location), location);
			if( t < old )
			{
				hold = p;
				old = t;
			}			
		}
		return hold;
	}
	
	public Point getFurthesPoint(double[] location, Shape shape)
	{
		double old = Double.MIN_VALUE;
		Point hold = null;
		for( Point p : this._points)
		{
			double t = Vector.distanceEuclid( shape.getNearestShadowPoint(
					p.getPosition(), location), location);
			if( t > old )
			{
				hold = p;
				old = t;
			}			
		}
		return hold;
	}
	
	public List<Spring> getSpringsToEvaluate()
	{
		LinkedList<Spring> out = new LinkedList<Spring>();
		for(Link l : _links)
		{
			/* avoid evaluating linear springs twice. TODO possibly we could come
			 * up with something a bit more straightforward.  */
			if( l.getMembers().size() == 2 )
			{
				for( Point p : this._points )
					if (((LinearSpring) l.getSpring()).isLeadingPoint(p))
						out.add( l.getSpring() );
			}
			else
				out.add( l.getSpring() );
		}
		out.add(this.spine);
		return out;
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
	public void update(double radius, double spineLength, AspectInterface i)
	{
		for(Link l : this._links)
		{
			l.initiate();
			for( AspectInterface a : l.getMembers() )
				if ( a != null && a.isAspect(AspectRef.removed))
					this._links.remove(l);
		}
		for ( Surface s: this._surfaces )
		{
			if (s instanceof Rod)
			{
				this.spine.setRestValue(spineLength);
				((Rod) s).setLength(spineLength);
				((Rod) s).setRadius(radius);
			}
			else if (s instanceof Ball)
			{
				this.spine = null;
				for(Link l : this._links)
				{
					Spring t = l.getSpring();
					if(t instanceof LinearSpring)
					{
						double out = 0;
						for( AspectInterface a : l.getMembers() )
							out += a.getDouble(AspectRef.bodyRadius);
						t.setRestValue(out);
					}
				}
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
	
	public List<Link> getLinks()
	{
		return this._links;
	}
	
	public void addLink(Link link)
	{
		this._links.add(link);
	}
	
	public void unLink(Link link)
	{
		this._links.remove(link);
	}
	
	public List<Surface> getSurfaces()
	{
		return this._surfaces;
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
	 * Simplify location - Body points are reduced by the minimum value in each
	 * dimension, in order to maintain relative distances, but simplify the
	 * location. This is used when agents move into a non-spatial compartment,
	 * in order to make clear that they do not retain their locations from
	 * spatial compartments, but to preserve morphology and distances between
	 * points
	 */
	public void simplifyLocation()
	{
		for (int i = 0; i < this.nDim(); i++)
		{
			double min = this._points.get(0).getPosition()[i];
			for (Point p : this._points)
			{
				if (p.getPosition()[i] < min)
					min = p.getPosition()[i];
			}
			for (Point p : this._points)
			{
				double[] position = p.getPosition().clone();
				double newCoordinate =  position[i] - min;
				position[i] = newCoordinate;
				p.setPosition(position);
			}
		}
	}
	
	/**
	 * returns a copy of this body and registers a new agent NOTE: does
	 * not set the agent's body state!
	 * TODO proper testing TODO make for multishape bodies
	 */
	public Body copy()
	{
		Body out;
		switch ( this._surfaces.get(0).type() )
		{
		case SPHERE:
			out = new Body(new Ball((Ball) this._surfaces.get(0)));
			out.constructBody();
			return out;
		case ROD:
			out = new Body(new Rod((Rod) this._surfaces.get(0)));
			out.constructBody();
			return out;
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
		
		for (Link l : this._links)
			modelNode.add(l.getModule());
		
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
	
	public void clearLinks() 
	{
		this._links.clear();
	}

}
