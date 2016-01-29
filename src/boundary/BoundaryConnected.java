/**
 * 
 */
package boundary;

import java.util.LinkedList;

import agent.Agent;

/**
 * \brief Abstract subclass of Boundary that connects Compartments.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class BoundaryConnected extends Boundary
{
	/**
	 * This boundary must be connected with another.
	 */
	protected BoundaryConnected _partnerBoundary;
	
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
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public BoundaryConnected()
	{
		super();
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS & SETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param aBoundary
	 */
	public void setPartnerBoundary(BoundaryConnected aBoundary)
	{
		this._partnerBoundary = aBoundary;
		/*
		 * Removed as caused stack overflow!
		 */
		//this._partnerBoundary.setPartnerBoundary(this);
	}
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * TODO Safety: check if agent is already in list?
	 * 
	 * @param agent
	 */
	public void addOutboundAgent(Agent agent)
	{
		this._departureLounge.add(agent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 */
	public void acceptInboundAgents(LinkedList<Agent> agents)
	{
		for ( Agent traveller : agents )
		{
			this._arrivalsLounge.add(traveller);
		}
	}
	
	/**
	 * 
	 */
	public void pushAllOutboundAgents()
	{
		this._partnerBoundary.acceptInboundAgents(this._departureLounge);
		this._departureLounge.clear();
	}
	
	
}
