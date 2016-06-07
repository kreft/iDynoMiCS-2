/**
 * 
 */
package boundary.library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import agent.Agent;
import boundary.Boundary;
import idynomics.AgentContainer;
import idynomics.Compartment;
import linearAlgebra.Vector;

/**
 * 
 * 
 */
public class ChemostatToChemostat extends Boundary
{
	/**
	 * TODO
	 */
	// TODO set this from protocol
	protected double _flowRate;
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();
	
	/**
	 * TODO
	 */
	protected double _agentsToDiluteTally = 0.0;
	
	/**
	 * TODO
	 * @param flowRate
	 */
	public void setFlowRate(double flowRate)
	{
		this._flowRate = flowRate;
	}
	
	/**
	 * @return Flow rate (units of volume per time).
	 */
	public double getFlowRate()
	{
		return this._flowRate;
	}
	
	/**
	 * TODO
	 * @param comp
	 */
	public void setPartnerCompartment(Compartment comp)
	{
		ChemostatToChemostat cIn = new ChemostatToChemostat();
		/* */
		cIn.setFlowRate( - this._flowRate );
		comp.getShape().addOtherBoundary(cIn);
		this.setPartner(cIn);
	}
	
	/**
	 * TODO
	 */
	@Override
	public Boundary makePartnerBoundary()
	{
		ChemostatToChemostat cIn = new ChemostatToChemostat();
		cIn.setFlowRate(this._flowRate);
		this._partner = cIn;
		return cIn;
	}
	
	/*************************************************************************
	 * SOLUTE TRANSFERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @return
	 */
	public double getConcentration(String name)
	{
		return this._concns.get(name);
	}
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
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
	public List<Agent> agentsToGrab(AgentContainer agentCont, double timeStep)
	{
		List<Agent> out = new LinkedList<Agent>();
		int nAllAgents = agentCont.getNumAllAgents();
		if ( (nAllAgents > 0) && (this._flowRate < 0.0) )
		{
			/* 
			 * This is an outflow: remember to subtract, since flow rate out
			 * is negative.
			 */
			this._agentsToDiluteTally -= this._flowRate * timeStep;
			int n = (int) this._agentsToDiluteTally;
			int[] nums = Vector.randomInts(n, 0, nAllAgents);
			for ( int i : nums )
				out.add(agentCont.chooseAgent(i));
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
