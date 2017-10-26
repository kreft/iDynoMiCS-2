package boundary.library;

import java.util.Collection;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.Boundary;
import boundary.standardBehaviours.DilutionAgentOutflowBehaviour;
import idynomics.Compartment;
import settable.Settable;

/**
 * \brief Connective boundary linking one dimensionless compartment to another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToChemostat extends Boundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 * 
	 * Encapsulated here as it is used by many other chemostat boundaries.
	 */
	private DilutionAgentOutflowBehaviour _agentOutflowBehaviour;

	public ChemostatToChemostat()
	{
		super();
		this._agentOutflowBehaviour = new DilutionAgentOutflowBehaviour();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		// TODO Auto-generated method stub
		
	}

	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return ChemostatToChemostat.class;
	}

	@Override
	public Boundary makePartnerBoundary()
	{
		ChemostatToChemostat cIn = 
				(ChemostatToChemostat) super.makePartnerBoundary();
		cIn.setVolumeFlowRate( - this._volumeFlowRate);
		return cIn;
	}

	/**
	 * \brief Make a partner boundary and set it to the compartment given.
	 * 
	 * @param comp
	 */
	// TODO consider deletion
	public void setPartnerCompartment(Compartment comp)
	{
		Boundary cIn = this.makePartnerBoundary();
		comp.addBoundary(cIn);
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void additionalPartnerUpdate() {}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	@Override
	public void addOutboundAgent(Agent anAgent)
	{
		/*
		 * Add the outbound agent to the departure lounge as normal, but also
		 * knock the dilution tally down by one.
		 */
		super.addOutboundAgent(anAgent);
		this._agentOutflowBehaviour.reduceTallyByOne();
	}

	@Override
	public Collection<Agent> agentsToGrab()
	{
		return this._agentOutflowBehaviour.agentsToGrab(
				this._agents, this.getDilutionRate());
	}
}
