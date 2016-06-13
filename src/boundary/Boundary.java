package boundary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlRef;
import generalInterfaces.XMLable;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;

/**
 * \brief General class of boundary for a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Boundary implements NodeConstructor
{
	/**
	 * XML tag for the name of the partner boundary.
	 */
	// TODO implement this in node construction
	public final static String PARTNER = XmlRef.boundaryPartner;
	/**
	 * The boundary this is connected with (not necessarily set).
	 */
	protected Boundary _partner;
	/**
	 * 
	 */
	// TODO implement this in node construction
	protected String _partnerCompartmentName;
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();
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
	protected static final Tier SOLUTE_LEVEL = Tier.DEBUG;
	/**
	 * Log verbosity level for debugging purposes (set to BULK when not using).
	 */
	protected static final Tier AGENT_LEVEL = Tier.DEBUG;

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	/**
	 * TODO
	 * @return
	 */
	public String getName()
	{
		return XmlRef.dimensionBoundary;
		// TODO return dimension and min/max for SpatialBoundary?
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	protected abstract Class<?> getPartnerClass();

	/**
	 * \brief Set the given boundary as this boundary's partner.
	 * 
	 * @param partner Boundary to use.
	 */
	public void setPartner(Boundary partner)
	{
		this._partner = partner;
	}

	/**
	 * @return {@code true} if this boundary still needs a partner boundary to
	 * be set, {@code false} if it already has one or does not need one at all.
	 */
	public boolean needsPartner()
	{
		return ( this._partnerCompartmentName != null ) &&
				( this._partner == null );
	}

	/**
	 * @return The name of the compartment this boundary should have a partner
	 * boundary with.
	 */
	public String getPartnerCompartmentName()
	{
		return this._partnerCompartmentName;
	}

	/**
	 * \brief Make a new {@code Boundary} that is the partner to this one.
	 * 
	 * @return New {@code Boundary} object with partner-partner links set.
	 */
	public Boundary makePartnerBoundary()
	{
		Boundary out = null;
		Class<?> bClass = this.getPartnerClass();
		if ( bClass != null )
		{
			try
			{
				out = (Boundary) bClass.newInstance();
				this.setPartner(out);
				out.setPartner(this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return out;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	/**
	 * \brief Get the concentration of a solute at this boundary.
	 * 
	 * @param name Name of the solute.
	 * @return Concentration of the solute.
	 */
	public double getConcentration(String name)
	{
		return this._concns.get(name);
	}

	/**
	 * \brief Set the concentration of a solute at this boundary.
	 * 
	 * @param name Name of the solute.
	 * @param concn Concentration of the solute.
	 */
	public void setConcentration(String name, double concn)
	{
		this._concns.put(name, concn);
	}

	/**
	 * \brief TODO
	 * 
	 * @param environment
	 */
	public abstract void updateConcentrations(EnvironmentContainer environment);

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	/**
	 * \brief Put the given agent into the departure lounge.
	 * 
	 * @param agent Agent to leave the compartment via this boundary.
	 */
	public void addOutboundAgent(Agent anAgent)
	{
		Log.out(AGENT_LEVEL, " - Accepting agent (ID: "+
				anAgent.identity()+") to departure lounge");
		this._departureLounge.add(anAgent);
	}

	/**
	 * \brief Put the given agent into the arrivals lounge.
	 * 
	 * @param anAgent Agent to enter the compartment via this boundary.
	 */
	public void acceptInboundAgent(Agent anAgent)
	{
		Log.out(AGENT_LEVEL, " - Accepting agent (ID: "+
				anAgent.identity()+") to arrivals lounge");
		this._arrivalsLounge.add(anAgent);
	}

	/**
	 * \brief Put the given agents into the arrivals lounge.
	 * 
	 * @param agents List of agents to enter the compartment via this boundary.
	 */
	public void acceptInboundAgents(List<Agent> agents)
	{
		Log.out(AGENT_LEVEL, "Boundary "+this.getName()+" accepting "+
				agents.size()+" agents to arrivals lounge");
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
			Log.out(AGENT_LEVEL, "Boundary "+this.getName()+" pushing "+
					this._departureLounge.size()+" agents to partner");
			this._partner.acceptInboundAgents(this._departureLounge);
			this._departureLounge.clear();
		}
	}

	// TODO delete once boundary gets full control of agent transfers
	public List<Agent> getAllInboundAgents()
	{
		return this._arrivalsLounge;
	}

	// TODO make protected once boundary gets full control of agent transfers
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

	/*************************************************************************
	 * XML-ABLE
	 ************************************************************************/

	// TODO replace with node construction

	public static Boundary getNewInstance(String className)
	{
		return (Boundary) XMLable.getNewInstance(className, "boundary.library.");
	}


	public boolean isReadyForLaunch()
	{
		// TODO
		return true;
	}

	/* ***********************************************************************
	 * NODE CONTRUCTION
	 * **********************************************************************/

	// TODO delete once nodeFactory has made this redundant
	public void init(Node xmlNode)
	{
		// TODO partner boundary name
	}

	@Override
	public ModelNode getNode()
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag(), this);

		// TODO
		// modelNode.requirement = Requirements.?

		// TODO

		return modelNode;
	}

	@Override
	public void setNode(ModelNode node)
	{
		// TODO
	}

	@Override
	public NodeConstructor newBlank()
	{
		// TODO
		return null;
	}

	// TODO ?
	//public void addChildObject(NodeConstructor childObject)

	@Override
	public String defaultXmlTag()
	{
		// FIXME use different tag for spatial/non-spatial boundaries?
		return XmlRef.dimensionBoundary;
	}
}
