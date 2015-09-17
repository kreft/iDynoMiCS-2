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
	protected LinkedList<Agent> _departureLounge;
	
	/**
	 * List of Agents that have travelled here from the connected compartment
	 * and need to be entered into this compartment.
	 */
	protected LinkedList<Agent> _arrivalsLounge;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public BoundaryConnected()
	{
		
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS & SETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * TODO Safety: check if agent is already in list?
	 * 
	 * @param agent
	 */
	public void acceptAgentLeaving(Agent agent)
	{
		this._departureLounge.add(agent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 */
	public void acceptTransferringAgents(LinkedList<Agent> agents)
	{
		for ( Agent traveller : agents )
			this._arrivalsLounge.add(traveller);
		agents.clear();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public LinkedList<Agent> getAgentsToTransfer()
	{
		return this._departureLounge;
	}
	
	
}
