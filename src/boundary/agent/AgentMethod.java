/**
 * 
 */
package boundary.agent;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import generalInterfaces.XMLable;
import idynomics.AgentContainer;
import idynomics.NameRef;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import shape.Shape;
import shape.ShapeConventions.DimName;
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
	
	/*************************************************************************
	 * ARRIVALS & DEPARTURES
	 ************************************************************************/
	
	/**
	 * \brief Enter the {@code Agent}s waiting in the arrivals lounge to the
	 * {@code AgentContainer}, using the spatial information given.
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 * @param dimN Name of the dimension whose boundary we are on.
	 * @param extreme Index of the dimension's extremes - must be 0 or 1.
	 */
	public abstract void agentsArrive(
			AgentContainer agentCont, DimName dimN, int extreme);
	
	/**
	 * \brief Enter the {@code Agent}s waiting in the arrivals lounge to the
	 * {@code AgentContainer}, without spatial information.
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 */
	public abstract void agentsArrive(AgentContainer agentCont);
	
	protected void placeAgentsRandom(
			AgentContainer agentCont, DimName dimN, int extreme)
	{
		Shape aShape = agentCont.getShape();
		double[] newLoc;
		for ( Agent anAgent : this._arrivalsLounge )
		{
			if ( AgentContainer.isLocated(anAgent) )
			{
				newLoc = aShape.getRandomLocationOnBoundary(dimN, extreme);
				Body body = (Body) anAgent.get(NameRef.agentBody);
				body.relocate(0, newLoc);
			}
			agentCont.addAgent(anAgent);
		}
	}
	
	/*************************************************************************
	 * methods to move into library, here for now
	 ************************************************************************/
	
	@Override
	public List<InputSetter> getRequiredInputs()
	{
		return new LinkedList<InputSetter>();
	}
	
	@Override
	public void acceptInput(String name, Object input)
	{
		/* Do nothing. */
	}
	
	@Override
	public void init(Element xmlElem)
	{
		
	}
	
	@Override
	public String getXml()
	{
		return null;
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	
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