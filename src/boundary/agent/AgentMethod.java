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
import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.XMLable;
import idynomics.AgentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import nodeFactory.ModelNode;
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
	 * ARRIVALS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 */
	public void acceptInboundAgent(Agent anAgent)
	{
		this._arrivalsLounge.add(anAgent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 */
	public void acceptInboundAgents(List<Agent> agents)
	{
		Log.out(Tier.DEBUG, 
				"Accepting "+agents.size()+"agents to arrivals lounge...");
		for ( Agent traveller : agents )
			this.acceptInboundAgent(traveller);
		Log.out(Tier.DEBUG, "   Done!");
	}
	
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
	
	/**
	 * \brief Helper method for placing agents in the arrivals lounge at random
	 * locations along the boundary surface described.
	 * 
	 * <p>Non-located agents are simply added to the agent container.</p>
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 * @param dimN Name of the dimension whose boundary we are on.
	 * @param extreme Index of the dimension's extremes - must be 0 or 1.
	 */
	protected void placeAgentsRandom(
			AgentContainer agentCont, DimName dimN, int extreme)
	{
		Shape aShape = agentCont.getShape();
		double[] newLoc;
		Body body;
		for ( Agent anAgent : this._arrivalsLounge )
		{
			if ( AgentContainer.isLocated(anAgent) )
			{
				newLoc = aShape.getRandomLocationOnBoundary(dimN, extreme);
				Log.out(Tier.DEBUG, "Placing agent (UID: "+anAgent.identity()+
						") at random location: "+Vector.toString(newLoc));
				body = (Body) anAgent.get(NameRef.agentBody);
				body.relocate(newLoc);
			}
			agentCont.addAgent(anAgent);
		}
	}
	
	// TODO delete once agent method gets full control of agent transfers
	public List<Agent> getAllInboundAgents()
	{
		return this._arrivalsLounge;
	}
	
	// TODO delete once agent method gets full control of agent transfers
	public void clearArrivalsLoungue()
	{
		this._arrivalsLounge.clear();
	}
	
	/*************************************************************************
	 * DEPARTURES
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 */
	public void addOutboundAgent(Agent anAgent)
	{
		// TODO Safety: check if agent is already in list?
		this._departureLounge.add(anAgent);
	}
	
	/**
	 * @return {@code true} if there is at least one outbound agent waiting in
	 * the departure lounge, {@code false} if the departure lounge is empty.
	 */
	public boolean hasOutboundAgents()
	{
		return (! this._departureLounge.isEmpty() );
	}
	
	/**
	 * \brief Push all agents in the departure lounge to the arrivals lounge of
	 * the given agent boundary method.
	 * 
	 * @param partner {@code Agent}-{@code Boundary} method that should accept
	 * the outbound agents.
	 */
	public void pushOutboundAgents(AgentMethod partner)
	{
		Log.out(Tier.DEBUG, "Pushing "+this._departureLounge.size()+
				" agents from departure lounge");
		partner.acceptInboundAgents(this._departureLounge);
		Log.out(Tier.DEBUG, "Clearing departure lounge");
		this._departureLounge.clear();
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
	
	// TODO required from xmlable interface
	public ModelNode getNode()
	{
		return null;
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