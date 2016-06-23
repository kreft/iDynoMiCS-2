package boundary.library;

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
	 * Flow rate (units of volume per time). Positive flow rate signifies
	 * inflow; negative signifies outflow.
	 */
	// TODO set this from protocol
	protected double _flowRate;

	/**
	 * Tally for the number of agents to be diluted via this boundary (kept at
	 * zero for inflows).
	 */
	protected double _agentsToDiluteTally = 0.0;

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	/**
	 * \brief Set this connective boundary's flow rate.
	 * 
	 * <p>Positive flow rate signifies inflow; negative signifies outflow.</p>
	 * 
	 * @param flowRate Flow rate (units of volume per time).
	 */
	public void setFlowRate(double flowRate)
	{
		this._flowRate = flowRate;
	}

	/**
	 * \brief Get this connective boundary's flow rate.
	 * 
	 * <p>Positive flow rate signifies inflow; negative signifies outflow.</p>
	 * 
	 * @return Flow rate (units of volume per time).
	 */
	public double getFlowRate()
	{
		return this._flowRate;
	}

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
		cIn.setFlowRate( - this._flowRate);
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
		comp.getShape().addOtherBoundary(cIn);
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void updateConcentrations()
	{
		/* Inflows have concentrations set by their partner. */
		if ( this._flowRate > 0.0 )
			return;
		/* This is an outflow. */
		double concn;
		for ( String name : this._environment.getSoluteNames() )
		{
			concn = this._environment.getAverageConcentration(name);
			this.setConcentration(name, concn);
			this._partner.setConcentration(name, concn);
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
	public List<Agent> agentsToGrab()
	{
		List<Agent> out = new LinkedList<Agent>();
		int nAllAgents = this._agents.getNumAllAgents();
		if ( (nAllAgents > 0) && (this._flowRate < 0.0) )
		{
			/* 
			 * This is an outflow: remember to subtract, since flow rate out
			 * is negative.
			 */
			this._agentsToDiluteTally -= this._flowRate * 
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
