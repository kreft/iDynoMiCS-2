package surface;

import java.util.LinkedList;
import java.util.List;

import aspect.AspectInterface;

/**
 * Some testing class will likely be removed or get an other form
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Link {

	protected List<AspectInterface> _members = new LinkedList<AspectInterface>();
	/**
	 * 
	 */
	protected List<Spring> _springs = new LinkedList<Spring>();

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
}
