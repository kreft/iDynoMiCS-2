/**
 * 
 */
package boundary.library;

import java.util.Collection;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.Boundary;
import boundary.standardBehaviours.DilutionAgentOutflowBehaviour;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * \brief The ChemostatOut boundary allows to set an outflow with a solute
 * concentration that equals the concentration in the chemostat.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatOut extends Boundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 * 
	 * Encapsulated here as it is used by many other chemostat boundaries.
	 */
	private DilutionAgentOutflowBehaviour _agentOutflowBehaviour;
	
	public ChemostatOut()
	{
		super();
		this._agentOutflowBehaviour = new DilutionAgentOutflowBehaviour();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {

		this.setVolumeFlowRate( Double.valueOf( XmlHandler.obtainAttribute( 
				xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag() ) ) );
	}
	
	@Override
	protected Class<?> getPartnerClass()
	{
		return null;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	
	private double getConcentration(String name)
	{
		return this._environment.getAverageConcentration(name);
	}
	
	@Override
	public double getMassFlowRate(String name)
	{
		return this.getConcentration(name) * this._volumeFlowRate;
	}
	
	@Override
	public void updateMassFlowRates()
	{
		for ( String name : this._environment.getSoluteNames() )
			this.setMassFlowRate(name, this.getMassFlowRate(name));
	}
	
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
