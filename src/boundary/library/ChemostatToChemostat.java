package boundary.library;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import agent.Agent;
import boundary.Boundary;
import idynomics.Compartment;
import idynomics.Idynomics;
import linearAlgebra.Vector;

/**
 * \brief Connective boundary linking one dimensionless compartment to another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToChemostat extends Boundary
{
	/**
	 * Tally for the number of agents to be diluted via this boundary (kept at
	 * zero for inflows).
	 */
	protected double _agentsToDiluteTally = 0.0;

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
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
			int n = (int) this._agentsToDiluteTally;
			/* Cannot dilute more agents than there are in the compartment. */
			n = Math.min(n, nAllAgents);
			int[] nums = Vector.randomInts(n, 0, nAllAgents);
			for ( int i : nums )
				out.add(this._agents.chooseAgent(i));
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
