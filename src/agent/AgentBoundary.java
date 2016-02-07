/**
 * 
 */
package agent;

import org.w3c.dom.Node;

/**
 * \brief TODO
 * 
 * NOTE: Bas [07.02.2016] all agent boundary interactions are done by Shape
 * aren't they? is this class needed?
 * 
 * @author Bastiaan Cockx
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class AgentBoundary
{
	public interface AgentMethod
	{
		void init(Node xmlNode);
		
		default boolean isCyclic()
		{
			return false;
		}
	}
	
	/*************************************************************************
	 * USEFUL SUBMETHODS
	 ************************************************************************/
	
	
	/*************************************************************************
	 * COMMON GRIDMETHODS
	 ************************************************************************/
	
	public static class SolidBoundary implements AgentMethod
	{
		public void init(Node xmlNode)
		{
			// TODO
		}
	}
	
	public static class CyclicBoundary implements AgentMethod
	{
		public void init(Node xmlNode)
		{
			// TODO
		}
		
		public boolean isCyclic()
		{
			return true;
		}
	}
}