package boundary;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Node;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlRef;
import generalInterfaces.Instantiatable;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
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
	 * Reference to the environment of the compartment this process belongs to.
	 * Contains a reference to the compartment shape.
	 */
	protected EnvironmentContainer _environment;
	/**
	 * Reference to the agents of the compartment this process belongs to.
	 * Contains a reference to the compartment shape.
	 */
	protected AgentContainer _agents;
	
	/**
	 * 
	 */
	protected String _compartmentName;
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
	
	protected int _iterLastUpdated = 
			Idynomics.simulator.timer.getCurrentIteration() - 1;
	/**
	 * \brief Rate of flow of bulk liquid across this boundary: positive for
	 * flow into the compartment this boundary belongs to, negative for flow
	 * out. Units of volume per time.
	 * 
	 * <p>For most boundaries this will likely be zero, i.e. no volume flow in
	 * either direction.</p>
	 */
	protected double _volumeFlowRate = 0.0;
	/**
	 * TODO
	 */
	protected Map<String,Double> _massFlowRate = new HashMap<String,Double>();
	/**
	 * Agents that are leaving this compartment via this boundary, and
	 * so need to travel to the connected compartment.
	 */
	protected Collection<Agent> _departureLounge = new LinkedList<Agent>();
	/**
	 * Agents that have travelled here from the connected compartment
	 * and need to be entered into this compartment.
	 */
	protected Collection<Agent> _arrivalsLounge = new LinkedList<Agent>();
	/**
	 * Log verbosity level for debugging purposes (set to BULK when not using).
	 */
	protected static final Tier SOLUTE_LEVEL = Tier.BULK;
	/**
	 * Log verbosity level for debugging purposes (set to BULK when not using).
	 */
	protected static final Tier AGENT_LEVEL = Tier.BULK;

	/**
	 * 
	 * @param environment
	 * @param agents
	 * @param compartmentName
	 */
	// TODO this needs to be called by Compartment
	public void init(EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		this._environment = environment;
		this._agents = agents;
		this._compartmentName = compartmentName;
	}
	
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
	 * \brief TODO
	 * 
	 * @return
	 */
	public double getVolumeFlowRate()
	{
		return this._volumeFlowRate;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param rate
	 */
	public void setVolumeFlowRate(double rate)
	{
		this._volumeFlowRate = rate;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public double getDilutionRate()
	{
		return this._volumeFlowRate / this._agents.getShape().getTotalVolume();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @return
	 */
	public double getMassFlowRate(String name)
	{
		if ( this._massFlowRate.containsKey(name) )
			return this._massFlowRate.get(name);
		return 0.0;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param rate
	 */
	public void setMassFlowRate(String name, double rate)
	{
		this._massFlowRate.put(name, rate);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param rate
	 */
	public void increaseMassFlowRate(String name, double rate)
	{
		this._massFlowRate.put(name, rate + this._massFlowRate.get(name));
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public void updateMassFlowRates()
	{
		/*
		 * 
		 */
		int currentIter = Idynomics.simulator.timer.getCurrentIteration();
		if ( this._partner == null )
		{
			this._iterLastUpdated = currentIter;
			// TODO check that we should do nothing here
			return;
		}
		if ( this._iterLastUpdated < currentIter )
		{
			double thisRate, partnerRate;
			for ( String name : this._environment.getSoluteNames() )
			{
				/*
				 * Store both rates prior to the switch.
				 */
				thisRate = this.getMassFlowRate(name);
				partnerRate = this._partner.getMassFlowRate(name);
				/*
				 * Apply any volume-change conversions.
				 */
				// TODO
				/*
				 * Apply the switch.
				 */
				this.setMassFlowRate(name, partnerRate);
				this._partner.setMassFlowRate(name, thisRate);
			}
			/*
			 * Update the iteration numbers so that the partner boundary
			 * doesn't reverse the changes we just made!
			 */
			this._iterLastUpdated = currentIter;
			this._partner._iterLastUpdated = currentIter;
		}
	}

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
		if ( Log.shouldWrite(AGENT_LEVEL) )
		{
			Log.out(AGENT_LEVEL, " - Accepting agent (ID: "+
					anAgent.identity()+") to departure lounge");
		}
		this._departureLounge.add(anAgent);
	}

	/**
	 * \brief Put the given agent into the arrivals lounge.
	 * 
	 * @param anAgent Agent to enter the compartment via this boundary.
	 */
	public void acceptInboundAgent(Agent anAgent)
	{
		if ( Log.shouldWrite(AGENT_LEVEL) )
		{
			Log.out(AGENT_LEVEL, " - Accepting agent (ID: "+
					anAgent.identity()+") to arrivals lounge");
		}
		this._arrivalsLounge.add(anAgent);
	}

	/**
	 * \brief Put the given agents into the arrivals lounge.
	 * 
	 * @param agents List of agents to enter the compartment via this boundary.
	 */
	public void acceptInboundAgents(Collection<Agent> agents)
	{
		if ( Log.shouldWrite(AGENT_LEVEL) )
		{
			Log.out(AGENT_LEVEL, "Boundary "+this.getName()+" accepting "+
					agents.size()+" agents to arrivals lounge");
		}
		for ( Agent anAgent : agents )
			this.acceptInboundAgent(anAgent);
		if ( Log.shouldWrite(AGENT_LEVEL) )
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
			if ( Log.shouldWrite(AGENT_LEVEL) )
			{
				Log.out(AGENT_LEVEL, "Boundary "+this.getName()+" pushing "+
						this._departureLounge.size()+" agents to partner");
			}
			this._partner.acceptInboundAgents(this._departureLounge);
			this._departureLounge.clear();
		}
	}

	// TODO delete once boundary gets full control of agent transfers
	public Collection<Agent> getAllInboundAgents()
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
	public void agentsArrive()
	{
		for ( Agent anAgent : this._arrivalsLounge )
			this._agents.addAgent(anAgent);
		this._arrivalsLounge.clear();
	}

	/**
	 * \brief Compile a list of the agents that this boundary wants to remove
	 * from the compartment and put into its departures lounge.
	 * 
	 * @param agentCont The {@code AgentContainer} that contains the 
	 * {@code Agent}s for selection.
	 * @return List of agents for removal.
	 */
	public Collection<Agent> agentsToGrab()
	{
		return new LinkedList<Agent>();
	}

	/* ***********************************************************************
	 * XML-ABLE
	 * **********************************************************************/

	// TODO replace with node construction

	public static Boundary getNewInstance(String className)
	{
		return (Boundary) Instantiatable.getNewInstance(className, "boundary.library.");
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
