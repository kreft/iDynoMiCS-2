/**
 * 
 */
package boundary;

import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlRef;
import dataIO.Log.Tier;
import idynomics.AgentContainer;

/**
 * \brief General class of boundary for a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Boundary
{
	// TODO move this to XmlLabel?
	public final static String DEFAULT_GM = "defaultGridMethod";
	
	public final static String PARTNER = XmlRef.boundaryPartner;
	
	/**
	 * The boundary this is connected with - not necessarily set.
	 */
	protected Boundary _partner;
	/**
	 * TODO
	 */
	protected String _partnerCompartmentName;
	
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
	
	/**
	 * Log verbosity level for debugging purposes (set to BULK when not using).
	 */
	private static final Tier AGENT_LEVEL = Tier.DEBUG;
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * TODO
	 * @return
	 */
	public String getName()
	{
		return XmlRef.dimensionBoundary;
		// TODO return dimension and min/max?
	}
	
	/**
	 * TODO
	 * @param partner
	 */
	public void setPartner(Boundary partner)
	{
		this._partner = partner;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public boolean needsPartner()
	{
		return ( this._partnerCompartmentName != null ) &&
				( this._partner == null );
	}
	
	/**
	 * TODO
	 * @return
	 */
	public String getPartnerCompartmentName()
	{
		return this._partnerCompartmentName;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public Boundary makePartnerBoundary()
	{
		return null;
	}
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param agent
	 */
	public void addOutboundAgent(Agent anAgent)
	{
		Log.out(AGENT_LEVEL, " - Accepting agent (ID: "+
				anAgent.identity()+") to departure lounge");
		this._departureLounge.add(anAgent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param anAgent
	 */
	public void acceptInboundAgent(Agent anAgent)
	{
		Log.out(AGENT_LEVEL, " - Accepting agent (ID: "+
				anAgent.identity()+") to arrivals lounge");
		this._arrivalsLounge.add(anAgent);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param agents
	 */
	public void acceptInboundAgents(List<Agent> agents)
	{
		Log.out(AGENT_LEVEL, "Boundary "+this.getName()+" accepting "+
				agents.size()+"agents to arrivals lounge");
		for ( Agent anAgent : agents )
			this.acceptInboundAgent(anAgent);
		Log.out(AGENT_LEVEL, " Done!");
	}
	
	/**
	 * Push all agents in the departure lounge to the partner boundary's
	 * arrivals lounge.
	 */
	public void pushAllOutboundAgents()
	{
		if ( this._partner == null )
		{
			if ( ! this._departureLounge.isEmpty() )
			{
				// TODO throw exception? Error message to log?
			}
		}
		else
		{
			this._partner.acceptInboundAgents(this._departureLounge);
			this._departureLounge.clear();
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
	
	/**
	 * \brief Enter the {@code Agent}s waiting in the arrivals lounge to the
	 * {@code AgentContainer}.
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 */
	public void agentsArrive(AgentContainer agentCont)
	{
		for ( Agent anAgent : this._arrivalsLounge )
			agentCont.addAgent(anAgent);
		this._arrivalsLounge.clear();
	}
	
	/**
	 * \brief Compile a list of the agents that this boundary wants to remove
	 * from the compartment and put into its departures lounge.
	 * 
	 * @param agentCont The {@code AgentContainer} that contains the 
	 * {@code Agent}s for selection.
	 * @param timeStep Length of the time period.
	 * @return List of agents for removal.
	 */
	public List<Agent> agentsToGrab(AgentContainer agentCont, double timeStep)
	{
		return new LinkedList<Agent>();
	}
}
