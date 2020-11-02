package surface;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import instantiable.Instance;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import utility.Helper;

/**
 * Some testing class will likely be removed or get an other form
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Link implements Instantiable, Settable  {

	protected List<AspectInterface> _members = new LinkedList<AspectInterface>();
	/**
	 * 
	 */
	protected List<Spring> _springs = new LinkedList<Spring>();
	
	protected List<Integer> _arriving = new LinkedList<Integer>(); 

	/**
	 * TODO
	 */
	protected double _snap;
	
	public Link()
	{
	}
	
	/**
	 * Add spring at specific position in case position indexing is used
	 * @param pos
	 * @param spring
	 */
	public void addSpring( int pos, Spring spring )
	{
		this._springs.add(pos, spring);
	}

	public void addSpring( Spring spring )
	{
		this._springs.add(spring);
	}
	
	public List<Spring> getSprings( )
	{
		return _springs;
	}
	
	public void addMember(  int pos, AspectInterface member )
	{
		if( this._members.size() > pos)
			this._members.remove(pos);
		this._members.add(pos, member);
	}
	
	public List<AspectInterface> getMembers()
	{
		return this._members;
	}
	
	public void update(int pos, Point point)
	{
		for(Spring s : _springs)
			s.setPoint(pos, point);
	}
	
	public void update()
	{
		if( !this._arriving.isEmpty() )
		{
			for (Integer e : this._arriving) 
			{
				this._members.add( Idynomics.simulator.findAgent( 
						Integer.valueOf( e ) ) );
			}
			LinkedList<Point> out = new LinkedList<Point>();
			for ( AspectInterface a : this._members)
			{
				out.add( ((Body) ((Agent) a).get(AspectRef.agentBody)).getPoints().get(0) );
			}
			for ( Spring s : this._springs)
			{
				for( int i = 0; i < out.size(); i++ )
				{
					s.setPoint(i, out.get(i));
				}
			}
		}
	}

	@Override
	public Module getModule() {
		Module modelNode = new Module(XmlRef.link, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
		
		for (Spring p : this._springs )
			modelNode.add(p.getModule() );

		for (AspectInterface a : this._members)
		{
			Module mem = new Module(XmlRef.member, this);
			mem.add(new Attribute(XmlRef.identity, 
					String.valueOf(((Agent) a).identity()), null, false));
			modelNode.add(mem);
		}
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
			Collection<Element> srpingNodes =
			XmlHandler.getAllSubChild(xmlElement, XmlRef.spring);
			for (Element e : srpingNodes) 
			{
				String type = e.getAttribute(XmlRef.typeAttribute);
				this._springs.add((Spring) Instance.getNew(type, null) );
			}
			
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
