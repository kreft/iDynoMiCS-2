/**
 * 
 */
package agent;

import org.w3c.dom.Node;

/**
 * \brief TODO
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
		
		default double[] getCyclicPoint(double[] location)
		{
			return null;
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
		
		public double[] getCyclicPoint(double[] location)
		{
			//TODO
			return null;
		}
	}
}