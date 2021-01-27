package surface.link;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import expression.Expression;
import idynomics.Idynomics;
import instantiable.Instantiable;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;
import surface.Point;
import utility.Helper;

/**
 * \brief: Link two or three points together with linear or torsion spring.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 */
public class Link implements Instantiable, Settable  {

	protected List<AspectInterface> _members = 
			new LinkedList<AspectInterface>();
	/**
	 * 
	 */
	protected Spring _spring;
	
	/**
	 * Refers to the agents associated with this link when the simulation is
	 * staged from XML. NOTE: some agents may not have constructed yet thus
	 * the link can only be established after all agents have been loaded in
	 * from XML.
	 */
	protected List<Integer> _arriving = new LinkedList<Integer>(); 

	/**
	 * TODO snap condition?
	 */
	protected double _snap;
	
	public Link()
	{
		
	}

	public void setSpring( Spring spring )
	{
		this._spring = spring;
	}
	
	public Spring getSpring( )
	{
		return _spring;
	}
	
	public void putMember(  int pos, AspectInterface member )
	{
		if( member == null )
			Log.out(Tier.CRITICAL, "attempted adding null member to Link");
		/* replace old member on same pos if any */
		if( this._members.size() > pos)
			this._members.remove(pos);
		this._members.add(pos, member);
	}
	
	public List<AspectInterface> getMembers()
	{
		return this._members;
	}

	/**
	 * Links with three agents represent torsion springs
	 * @param a
	 * @param b
	 * @param c
	 */
	public static void torsion(Agent a, Agent b, Agent c)
	{
		Link link = new Link();
		torsion(a,b,c, link);
		link.putMember(0, a);
		link.putMember(1, b);
		link.putMember(2, c);
		((Body) b.get( AspectRef.agentBody) ).addLink(link);
	}
	
	/**
	 * Links with three agents represent torsion springs
	 * @param a
	 * @param b
	 * @param c
	 * @param link
	 */
	public static void torsion(Agent a, Agent b, Agent c, Link link)
	{

		Shape shape = a.getCompartment().getShape();

		Body aBody = (Body) a.get(AspectRef.agentBody);
		Body bBody = (Body) b.get(AspectRef.agentBody);
		Body cBody = (Body) c.get(AspectRef.agentBody);
		Double linkerStiffness = (double) b.getOr( 
				AspectRef.torsionStiffness, 0.0);
		if( Log.shouldWrite(Tier.NORMAL) && linkerStiffness == 0.0 )
			Log.out(Tier.NORMAL, "Warning: torsion stiffness equals zero.");
		/* FIXME placeholder default function */

		Expression springFun = (Expression) b.get(
				AspectRef.torsionFunction );
		
		if( springFun == null )
			Log.out(Tier.CRITICAL, "Warning: spring function not set.");
		
		Point[] points = null;
		if( a != b && b != c)
		{
			/* b must be coccoid */
			points = new Point[] { aBody.getClosePoint( bBody.getCenter(shape), 
					shape ), bBody.getPoints().get(0), cBody.getClosePoint(
					bBody.getCenter(shape), shape) };
		}
		else if ( a == b)
		{
			/* b is rod with a */
			points = new Point[] { aBody.getFurthesPoint(cBody.getCenter(shape),
					shape), bBody.getClosePoint( cBody.getCenter(shape), shape), 
					cBody.getClosePoint( bBody.getCenter(shape), shape) };
		}
		else
		{
			/* b is rod with c */
			points = new Point[] { aBody.getClosePoint( bBody.getCenter(shape), 
					shape), bBody.getClosePoint(aBody.getCenter(shape), shape), 
					cBody.getFurthesPoint(aBody.getCenter(shape), shape) };
		}
		
		Spring spring = new TorsionSpring(linkerStiffness, points, springFun,
				Math.PI);
		link.setSpring(spring);

	}
	
	public static void linear(Agent a, Agent b)
	{
		Body momBody = (Body) a.get(AspectRef.agentBody);
		Body daughterBody = (Body) b.get(AspectRef.agentBody);
		Link link = new Link();
		linear(a, b, link);
		link.putMember(0, a);
		link.putMember(1, b);
		momBody.addLink(link);
		/* to keep consistent with XML out make this a copy */
		daughterBody.addLink(link); 
	}
	
	public static void linear(Agent a, Agent b, Link link)
	{
		Shape shape = a.getCompartment().getShape();
		if(a == null || b == null)
			return;
		Body momBody = (Body) a.get(AspectRef.agentBody);
		Body daughterBody = (Body) b.get(AspectRef.agentBody);
		
		if( !link._members.isEmpty())
		{
			for( Link l : daughterBody.getLinks() )
			{
				if( l._members.size() == 2 && l._members.contains(a) && 
						l._members.contains(b))
				{
					daughterBody.unLink(l);
					daughterBody.addLink(link);
				}
			}
		}
		Double linkerStiffness = (double) b.getOr( 
				AspectRef.linearStiffness, 0.0);
		
		if( Log.shouldWrite(Tier.NORMAL) && linkerStiffness == 0.0 )
			Log.out(Tier.NORMAL, "Warning: linker stiffness equals zero. ");
		
		Expression springFun = (Expression) b.get(
				AspectRef.linearFunction );
		
		if( springFun == null )
			Log.out(Tier.CRITICAL, "Warning: spring function not set.");

		Point[] points = new Point[] { momBody.getClosePoint(
				daughterBody.getCenter(shape), shape), 
				daughterBody.getClosePoint(
				momBody.getCenter(shape), shape) };
		
		double restlength;
		if(a != b )
			restlength = a.getDouble(AspectRef.bodyRadius) + 
			b.getDouble(AspectRef.bodyRadius);
		else
			{
			restlength = 1;
			}
		Spring spring = new LinearSpring(linkerStiffness, points, springFun,
				restlength);
		
		link.setSpring(spring);
	}
	
	public void update()
	{
		if( getMembers().size() == 2 && 
				getMembers().get(0) != getMembers().get(1)
				)
		{
			getSpring().setRestValue( 
					getMembers().get(0).getDouble(AspectRef.bodyRadius)
					+  getMembers().get(1).getDouble(AspectRef.bodyRadius) );
		}
		if( this._spring instanceof LinearSpring)
		{
			boolean c = false, d = false;
			LinearSpring s = (LinearSpring) this._spring;
			for( AspectInterface member : getMembers() )
			{
				Body b = (Body) member.getValue(AspectRef.agentBody);
				if ( b.getPoints().contains( s._a ))
					c = true;
				if ( b.getPoints().contains( s._b ))
					d = true;
			}
			if( !c || !d)
			{
				Log.out( this.getClass().getSimpleName() + " point mismatch" );
			}
		}
	}
	public void setPoint(int pos, Point point)
	{
		this._spring.setPoint(pos, point, false);
	}
	
	public void setPoint(int pos, Point point, boolean tempDuplicate)
	{
		this._spring.setPoint(pos, point, tempDuplicate);
	}
	
	public void initiate()
	{
		if( !this._arriving.isEmpty() )
		{
			for (int i = 0; i< this._arriving.size(); i++) 
			{
				AspectInterface m = Idynomics.simulator.findAgent( 
						Integer.valueOf( this._arriving.get(i)) );
				if( m != null )
					this._members.add( i,  m);
				else
					Log.out("unkown agent " +i+ " in " + 
							this.getClass().getSimpleName());
			}
			if( this._members.size() == 2)
			{
				linear((Agent) this._members.get(0), 
						(Agent) this._members.get(1),this);
			}
			else if ( this._members.size() == 3)
			{
				torsion((Agent) this._members.get(0), 
						(Agent) this._members.get(1),
						(Agent) this._members.get(2),this);
			}
			this._arriving.clear();
		}
	}

	@Override
	public Module getModule() {
		Module modelNode = new Module(XmlRef.link, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);

		for (int i = 0; i < this._members.size(); i++)
		{
			Module mem = new Module(XmlRef.member, this);
			mem.add(new Attribute(XmlRef.identity, String.valueOf((
					(Agent) this._members.get(i)).identity()), null, false ));
			modelNode.add(mem);
		}
		
		/* NOTE springs follow from link initiation */
		
		return modelNode;
	}

	@Override
	public String defaultXmlTag() {
		return XmlRef.link;
	}

	@Override
	public void setParent(Settable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Settable getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		if( !Helper.isNullOrEmpty( xmlElement ))
		{			
			/* find member agents and add them to the member list. */
			Collection<Element> memberNodes =
			XmlHandler.getAllSubChild(xmlElement, XmlRef.member);
			for (Element e : memberNodes) 
			{
				this._arriving.add( 
						Integer.valueOf(e.getAttribute(XmlRef.identity) ) );
			}
		
		}
	}
}
