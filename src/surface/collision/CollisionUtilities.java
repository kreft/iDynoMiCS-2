package surface.collision;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import referenceLibrary.AspectRef;
import surface.Surface;

public class CollisionUtilities {

	/**
	 * Note that the surface needs to be initiated before passing to this method
	 * use surface.init( collisionObject );
	 * @param surface
	 * @param agents
	 * @return
	 */
	public static List<Agent> getCollidingAgents(Surface surface, 
			Collection<Agent> agents)
	{
		List<Agent> nhbs = new LinkedList<Agent>();
		nhbs = new LinkedList<Agent>();
		for ( Agent a : agents )
		{
			boolean keep = false;
			for (Surface s : (List<Surface>) ((Body) 
					a.get(AspectRef.agentBody)).getSurfaces())
			{
				if ( surface.collisionWith(s) )
				{
					keep = true;
					break;
				}
			}
			if ( keep )
				nhbs.add(a);
		}
		return nhbs;
	}
}
