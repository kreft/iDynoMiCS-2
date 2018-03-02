package boundary;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.library.ChemostatToBoundaryLayer;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import instantiable.Instantiable;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import utility.Helper;

/**
 * \brief General class of boundary for a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public abstract class Boundary implements Settable, Instantiable
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
	public AgentContainer _agents;
	/**
	 * XML tag for the name of the partner boundary.
	 */
	// TODO implement this in node construction
	public final static String PARTNER = XmlRef.partnerCompartment;
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
	 * \brief Rates of flow of mass across this boundary: positive for
	 * flow into the compartment this boundary belongs to, negative for flow
	 * out. Map keys are solute names. Units of mass (or mole) per time.
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
	 * TODO
	 */
	private Settable _parentNode;
	/**
	 * Log verbosity level for debugging purposes (set to BULK when not using).
	 */
	protected static final Tier SOLUTE_LEVEL = Tier.BULK;
	/**
	 * Log verbosity level for debugging purposes (set to BULK when not using).
	 */
	protected static final Tier AGENT_LEVEL = Tier.DEBUG;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public void instantiate(Element xmlElement, Settable parent) 
	{
		this.setParent(parent);
		/* 
		 * If this class of boundary needs a partner, find the name of the
		 * compartment it connects to.
		 */
		if ( this.getPartnerClass() != null )
		{
			this._partnerCompartmentName = XmlHandler.obtainAttribute(
							xmlElement,
							PARTNER,
							XmlRef.dimensionBoundary);
		}
	}

	public boolean isReadyForLaunch()
	{
		if ( this._environment == null || this._agents == null )
			return false;
		return true;
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	/**
	 * @return The name of this boundary.
	 */
	public String getName()
	{
		return XmlRef.dimensionBoundary;
		// TODO return dimension and min/max for SpatialBoundary?
	}

	/**
	 * \brief Tell this boundary what it needs to know about the compartment it
	 * belongs to.
	 * 
	 * @param environment The environment container of the compartment.
	 * @param agents The agent container of the compartment.
	 */
	public void setContainers(EnvironmentContainer environment, 
			AgentContainer agents)
	{
		this._environment = environment;
		this._agents = agents;
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	/**
	 * @return The class of boundary that can be a partner of this one, making
	 * a connection between compartments.
	 */
	public abstract Class<?> getPartnerClass();

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
		return ( this.getPartnerClass() != null ) &&
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
	 * \brief Get the volume flow rate for this boundary.
	 * 
	 * <p>This will likely be zero for non-connected boundaries, and also many
	 * connected boundaries.</p>
	 * 
	 * <p>A positive rate means flow into the compartment this boundary belongs
	 * to; a negative rate means fluid is flowing out.</p>
	 * 
	 * @return Rate of volume flow, in units of volume per time.
	 */
	public double getVolumeFlowRate()
	{
		return this._volumeFlowRate;
	}
	
	/**
	 * \brief Set the volume flow rate for this boundary.
	 * 
	 * <p>This should be unused for most boundaries.</p>
	 * 
	 * <p>A positive rate means flow into the compartment this boundary belongs
	 * to; a negative rate means fluid is flowing out.</p>
	 * 
	 * @param rate Rate of volume flow, in units of volume per time.
	 */
	public void setVolumeFlowRate(double rate)
	{
		this._volumeFlowRate = rate;
	}
	
	/**
	 * \brief Get the dilution rate for this boundary.
	 * 
	 * <p>The dilution rate is the volume flow rate, normalised by the volume
	 * of the compartment.</p>
	 * 
	 * @return Dilution rate, in units of per time.
	 */
	public double getDilutionRate()
	{
		return this._volumeFlowRate / this._agents.getShape().getTotalVolume();
	}
	
	/**
	 * \brief Get the mass flow rate of a given solute across this boundary.
	 * 
	 * <p>A positive rate means flow into the compartment this boundary belongs
	 * to; a negative rate means the solute is flowing out.</p>
	 * 
	 * @param name Name of the solute.
	 * @return Rate of mass flow, in units of mass (or mole) per time.
	 */
	public double getMassFlowRate(String name)
	{
		if ( this._massFlowRate.containsKey(name) )
			return this._massFlowRate.get(name);
		return 0.0;
	}
	
	/**
	 * \brief Set the mass flow rate of a given solute across this boundary.
	 * 
	 * <p>A positive rate means flow into the compartment this boundary belongs
	 * to; a negative rate means the solute is flowing out.</p>
	 * 
	 * @param name Name of the solute to set.
	 * @param rate Rate of mass flow, in units of mass (or mole) per time.
	 */
	public void setMassFlowRate(String name, double rate)
	{
		this._massFlowRate.put(name, rate);
	}
	
	/**
	 * \brief Increase the mass flow rate of a given solute across this
	 * boundary by a given amount.
	 * 
	 * <p>A positive rate means flow into the compartment this boundary belongs
	 * to; a negative rate means the solute is flowing out.</p>
	 * 
	 * @param name Name of the solute to set.
	 * @param rate Extra rate of mass flow, in units of mass (or mole) per time.
	 */
	public void increaseMassFlowRate(String name, double rate)
	{
		this._massFlowRate.put(name, rate + this._massFlowRate.get(name));
	}
	
	/**
	 * Reset all mass flow rates back to zero, for each solute in the
	 * environment.
	 */
	public void resetMassFlowRates()
	{
		for ( String name : this._environment.getSoluteNames() )
			this._massFlowRate.put(name, 0.0);
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
			 * If there is any additional updating to do, do it now.
			 */
			this.additionalPartnerUpdate();
			/*
			 * Update the iteration numbers so that the partner boundary
			 * doesn't reverse the changes we just made!
			 */
			this._iterLastUpdated = currentIter;
			this._partner._iterLastUpdated = currentIter;
		}
	}
	
	/**
	 * Method for doing any additional pre-step updates that are specific to
	 * the concrete sub-class of Boundary.
	 */
	public abstract void additionalPartnerUpdate();

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
		{
			this.acceptInboundAgent(anAgent);
		}
		if ( Log.shouldWrite(AGENT_LEVEL) )
			Log.out(AGENT_LEVEL, " Done!");
	}

	/**
	 * Push all agents in the departure lounge to the partner boundary's
	 * arrivals lounge.
	 */
	public void pushAllOutboundAgents()
	{
		if ( ! this._departureLounge.isEmpty() )
		{
			if ( this._partner == null )
			{
				if ( Log.shouldWrite(Tier.EXPRESSIVE) )
				{
					Log.out(Tier.EXPRESSIVE, "Boundary "+this.getName()+" removing "+
							this._departureLounge.size()+" agents");
				}
				for( Agent a : this._departureLounge )
					this._agents.registerRemoveAgent(a);
				this._departureLounge.clear();
			}
			else
			{
				if ( Log.shouldWrite(Tier.EXPRESSIVE) )
				{
					Log.out(Tier.EXPRESSIVE, "Boundary "+this.getName()+" pushing "+
							this._departureLounge.size()+" agents to partner");
				}
				Random randomSelector = new Random();
				Collection<Agent> acceptanceLounge = new LinkedList<Agent>();
				//Agent[] departureArray = (Agent[]) this._departureLounge.toArray();
				int numAgentsDepart = this._departureLounge.size();
				if (this.getPartnerClass() == BiofilmBoundaryLayer.class)
				{
					String partnerCompName = this.getPartnerCompartmentName();
					Compartment partnerComp = Idynomics.simulator.getCompartment(partnerCompName);
					double scFac = partnerComp.getScalingFactor();
					if (scFac == 1)
						this._partner.acceptInboundAgents(this._departureLounge);
					else {
						int numAgentsToAccept = (int) Math.ceil(numAgentsDepart / scFac);
						for (int i = 0; i < numAgentsToAccept; i++)
						{
							int agentIndex = i;
							if (agentIndex >= numAgentsDepart)
								agentIndex = randomSelector.nextInt(numAgentsDepart);
							Agent accepted = (Agent) this._departureLounge.toArray()[agentIndex];
							Agent acceptedCopy = new Agent(accepted);
							acceptedCopy.set(AspectRef.isLocated, true);
							acceptanceLounge.add(acceptedCopy);
						}
						this._partner.acceptInboundAgents(acceptanceLounge);
					}
				}
				else if (this.getPartnerClass() == ChemostatToBoundaryLayer.class)
				{
					Compartment thisComp = (Compartment) this._environment.getParent();
					double scFac = thisComp.getScalingFactor();
					if (scFac == 1)
						this._partner.acceptInboundAgents(this._departureLounge);
					else {
						int numAgentsToAccept = (int) Math.ceil(numAgentsDepart * scFac);
						for (int i = 0; i < numAgentsToAccept; i++)
						{
							int agentIndex = i;
							if (agentIndex >= numAgentsDepart)
								agentIndex = randomSelector.nextInt(numAgentsDepart);
							Agent accepted = (Agent) this._departureLounge.toArray()[agentIndex];
							Agent acceptedCopy = new Agent(accepted);
							acceptedCopy.set(AspectRef.isLocated, false);
							acceptanceLounge.add(acceptedCopy);
						}
					}
					this._partner.acceptInboundAgents(acceptanceLounge);
				}
				else
				{
					this._partner.acceptInboundAgents(this._departureLounge);
				}
				for( Agent a : this._departureLounge )
					this._agents.registerRemoveAgent(a);
				this._departureLounge.clear();
			}
		}
	}

	// TODO delete once boundary gets full control of agent transfers
	public Collection<Agent> getAllInboundAgents()
	{
		return this._arrivalsLounge;
	}

	/**
	 * Take all agents out from the arrivals lounge.
	 */
	protected void clearArrivalsLounge()
	{
		this._arrivalsLounge.clear();
	}

	/**
	 * \brief Enter the {@code Agent}s waiting in the arrivals lounge to the
	 * {@code AgentContainer}.
	 * 
	 * <p>This method will be overwritten by many sub-classes of Boundary,
	 * especially those that are sub-classes of SpatialBoundary.</p>
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 */
	public void agentsArrive()
	{
		for ( Agent anAgent : this._arrivalsLounge )
			this._agents.addAgent(anAgent);
		this.clearArrivalsLounge();
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
	 * NODE CONTRUCTION
	 * **********************************************************************/

	/* NOTE Bas: I found this note here: "delete once nodeFactory has made this 
	 * redundant" To be clear: do not delete this, in order for the node factory
	 * to work this needs to be implemented properly! Make sure your object
	 * implements the instantiatable interface and DO NOT bypass its 
	 * newNewInstance method.
	 */
	
	@Override
	public Module getModule()
	{
		Module modelNode = new Module(this.defaultXmlTag(), this);
		modelNode.add(new Attribute(XmlRef.classAttribute,
				this.getClass().getSimpleName(),
				null, true));
		/* Partner compartment. */
		if ( this.needsPartner() )
		{
			List<String> cList = Idynomics.simulator.getCompartmentNames();
			String[] cArray = Helper.listToArray(cList);
			modelNode.add(new Attribute(
					XmlRef.partnerCompartment,
					this._partnerCompartmentName, 
					cArray,
					true));
		}
		// TODO
		// modelNode.requirement = Requirements.?
		return modelNode;
	}

	@Override
	public void setModule(Module node)
	{
		// TODO
	}

	// TODO ?
	//public void addChildObject(NodeConstructor childObject)

	@Override
	public String defaultXmlTag()
	{
		// FIXME use different tag for spatial/non-spatial boundaries?
		return XmlRef.dimensionBoundary;
	}
	
	public void setParent(Settable parent)
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}
