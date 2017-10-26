/**
 * 
 */
package boundary.library;

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.Boundary;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief The ChemostatOut boundary allows to set an outflow with a solute
 * concentration that equals the concentration in the chemostat.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ChemostatOut extends Boundary
{
	private boolean constantVolume = false;
	protected double _agentsToDiluteTally = 0.0;
	protected boolean _agentRemoval = false;

	public ChemostatOut()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		if (! XmlHandler.hasAttribute(xmlElement, XmlRef.constantVolume))
			this.setVolumeFlowRate( Double.valueOf( XmlHandler.obtainAttribute( 
					xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag() ) ) );
		else
			this.constantVolume = true;
		this._agentRemoval = Helper.setIfNone( Boolean.valueOf( 
				XmlHandler.gatherAttribute( xmlElement, XmlRef.agentRemoval ) ), 
				false);
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
		if (this.constantVolume)
		{
			double totalOutFlow = 0.0;
			Iterator<Boundary> otherBounds = this._environment.getNonSpatialBoundaries().iterator();
			while (otherBounds.hasNext())
			{
				Boundary bound = otherBounds.next();
				if (bound != this)
					totalOutFlow -= bound.getVolumeFlowRate();
			}
			this.setVolumeFlowRate(totalOutFlow);
		}
		return this._environment.getAverageConcentration(name) * this._volumeFlowRate;

	}
	
	@Override
	public void updateMassFlowRates()
	{
		if (this.constantVolume)
		{
			double totalOutFlow = 0.0;
			Iterator<Boundary> otherBounds = this._environment.getShape().getAllBoundaries().iterator();
			while (otherBounds.hasNext())
			{
				Boundary bound = otherBounds.next();
				if (bound != this)
					totalOutFlow -= bound.getVolumeFlowRate();
			}
			this.setVolumeFlowRate(totalOutFlow);
		}
		for ( String name : this._environment.getSoluteNames() )
		{
			this._massFlowRate.put(name, 
					this.getConcentration(name) * this._volumeFlowRate);
		}
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
		this._agentsToDiluteTally--;
	}

	@Override
	public Collection<Agent> agentsToGrab()
	{
		List<Agent> out = new LinkedList<Agent>();
		int nAllAgents = this._agents.getNumAllAgents();
		if ( (nAllAgents > 0) && (this._volumeFlowRate < 0.0) )
		{
			/* 
			 * This is an outflow: remember to subtract, since dilution out
			 * is negative.
			 */
			this._agentsToDiluteTally -= this.getDilutionRate() * 
					Idynomics.simulator.timer.getTimeStepSize();
			
			if ( _agentRemoval )
			{
				/*
				 * dA/dt = rA
				 * A(t) = A(0) * e^(rt)
				 */
				double e = Math.exp( ( this.getDilutionRate() * 
						Idynomics.simulator.timer.getTimeStepSize() ) ); 
				for ( int i = 0; i < nAllAgents; i++ )
					if( ExtraMath.getUniRandDbl() > e )
					{
						Agent a = this._agents.chooseAgent(i);
						out.add(a);
						this._agents.registerRemoveAgent(a);
						if ( Log.shouldWrite(Tier.NORMAL) )
							Log.out(Tier.NORMAL, "Washed out agent");
					}
			}
		}
		else
		{
			/*
			 * If the agent container is empty, set the tally to zero: we
			 * shouldn't accumulate a high tally while the compartment is
			 * waiting for agents to arrive.
			 * 
			 * If the flow rate is positive, this is an inflow and so no agents
			 * to remove.
			 */
			this._agentsToDiluteTally = 0.0;
		}
		return out;
	}
}
