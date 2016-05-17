/**
 * 
 */
package boundary.agent;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import agent.Agent;
import generalInterfaces.XMLable;
import idynomics.Compartment;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import surface.Surface;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class AgentMethod implements IsSubmodel, XMLable
{
	
	/**
	 * List of Agents that are leaving this compartment via this boundary, and
	 * so need to travel to the connected compartment.
	 */
	protected LinkedList<Agent> _departureLounge = new LinkedList<Agent>();
	
	/**
	 * List of Agents that have travelled here from the connected compartment
	 * and need to be entered into this compartment.
	 */
	protected LinkedList<Agent> _arrivalsLounge = new LinkedList<Agent>();
	
	public abstract void agentsArrive(Compartment comp, Surface surf);
	
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
				AgentMethodLibrary.class.getDeclaredClasses());
	}
	
	
	public static AgentMethod getNewInstance(String className)
	{
		return (AgentMethod) XMLable.getNewInstance(className, 
										"boundary.agent.AgentMethodLibrary$");
	}
	
	
	public static class AgentMethodMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -4571936613706733683L;
		
		/**\brief TODO
		 * 
		 * @param name
		 * @param req
		 * @param target
		 */
		public AgentMethodMaker(String name, Requirement req, IsSubmodel target)
		{
			super(name, req, target);
		}
		
		@Override
		protected void doAction(ActionEvent e)
		{
			String name;
			if ( e == null )
				name = "";
			else
				name = e.getActionCommand();
			this.addSubmodel(AgentMethod.getNewInstance(name));
		}
		
		@Override
		public Object getOptions()
		{
			return AgentMethod.getAllOptions();
		}
	}
}