package boundary.standardBehaviours;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.Idynomics;
import linearAlgebra.Vector;

/**
 * \brief Commonly-used behaviour for diluting out agents via chemostat
 * boundaries.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class DilutionAgentOutflowBehaviour
{
	/**
	 * Tally for the number of agents to be diluted via this boundary (kept at
	 * zero for inflows).
	 */
	protected double _agentsToDiluteTally;
	
	public DilutionAgentOutflowBehaviour()
	{
		this._agentsToDiluteTally = 0.0;
	}
	
	/**
	 * \brief Compile a list of the agents that the boundary wants to remove
	 * from the compartment and put into its departures lounge.
	 * 
	 * @param agents AgentContainer
	 * @param dilutionRate Dilution rate (volume flow rate divided by volume)
	 * in same time units as the simulation timer.
	 * @return Collection of Agents to be removed to to dilution.
	 */
	public Collection<Agent> agentsToGrab(
			AgentContainer agents, double dilutionRate)
	{
		List<Agent> out = new LinkedList<Agent>();
		int nAllAgents = agents.getNumAllAgents();
		if ( (nAllAgents > 0) && (dilutionRate < 0.0) )
		{
			/* 
			 * This is an outflow: remember to subtract, since dilution out
			 * is negative.
			 */
			this._agentsToDiluteTally -= dilutionRate * 
					Idynomics.simulator.timer.getTimeStepSize();
			int n = (int) this._agentsToDiluteTally;
			/* Cannot dilute more agents than there are in the compartment. */
			n = Math.min(n, nAllAgents);
			int[] nums = Vector.randomInts(n, 0, nAllAgents);
			for ( int i : nums )
				out.add( agents.chooseAgent(i) );
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
	
	/**
	 * Reduce this behaviour's internal counter by one to signify that an agent
	 * has been successfully removed from the container.
	 */
	public void reduceTallyByOne()
	{
		this._agentsToDiluteTally--;
	}
}
