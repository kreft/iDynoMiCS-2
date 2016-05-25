package processManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import agent.Agent;
import boundary.Boundary;
import boundary.BoundaryLibrary.ChemostatInflow;
import boundary.BoundaryLibrary.ChemostatOutflow;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.ODEderivatives;
import utility.ExtraMath;

/**
 * Tool set for {@code ProcessManager}s that control processes suited to the
 * chemostat environment.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK
 */
public final class PMToolsChemostat
{
	// TODO This tool set needs to incorporate chemostat-biofilm interactions,
	// i.e. solute flows and agent migrations.
	
	// TODO this tool set needs to update the solute concentrations of 
	// out-flowing connection boundaries.
	
	/*************************************************************************
	 * SOLUTE DYNAMICS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteNames List of solute names for which the ODE solver is
	 * responsible.
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 * @return
	 */
	// TODO Rename this method? Potentially confused with the standard
	// deviation in statistics
	public static ODEderivatives standardDerivs(String[] soluteNames,
					EnvironmentContainer environment, AgentContainer agents)
	{
		return new ODEderivatives()
		{
			/**
			 * Temporary dictionary of all solute concentrations in the
			 * environment, and <i>not just those handled by this process
			 * manager.</i>
			 */
			Map<String, Double> concns;
			/**
			 * Dilution rate in units of time<sup>-1</sup>.
			 */
			double dilution = getDilutionRate(environment);
			/**
			 * Vector of inflow rates, in units of concentration per unit time. 
			 */
			double[] dYdTinflow = getInflows(soluteNames, environment);
			
			@Override
			public void firstDeriv(double[] destination, double[] y)
			{
				/*
				 * First deal with inflow and dilution: dSdT = D(Sin - S)
				 */
				Vector.timesTo(destination, y, - dilution);
				Vector.addEquals(destination, dYdTinflow);
				/*
				 * For the reactions, we will need the concentrations in
				 * dictionary format.
				 */
				concns = environment.getAverageConcentrations();
				for ( int i = 0; i < soluteNames.length; )
					concns.put(soluteNames[i], y[i]);
				/*
				 * Apply agent reactions. Note that any agents without
				 * reactions will return null, and so will be skipped.
				 */
				applyAgentReactions(destination, agents, concns, soluteNames);
				/*
				 * Apply extracellular reactions.
				 */
				for ( Reaction aReac : environment.getReactions() )
				{
					applyProductionRates(
									destination, aReac, concns, soluteNames);
				}
			}
			
			// TODO second derivatives?
			
			// TODO Jacobian matrix?
		};
	}
	
	
	/**
	 * \brief Get the total dilution rate of a chemostat {@code Compartment}.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @return Total dilution rate, in units of time<sup>-1</sup>.
	 */
	private static double getDilutionRate(EnvironmentContainer environment)
	{
		double out = 0.0;
		double rate;
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
			if ( aBoundary instanceof ChemostatOutflow )
			{
				rate = ((ChemostatOutflow) aBoundary).getFlowRate();
				if ( rate < 0.0 )
					out -= rate;
			}
		return out;
	}
	
	/**
	 * \brief Get the inflow rates for a list of solutes into this chemostat
	 * {@code Compartment}.
	 * 
	 * @param soluteNames List of solute names.
	 * @param environment The environment of a {@code Compartment}.
	 * @return List of inflow rates, in the same order as <b>soluteNames</b>,
	 * in units of concentration per unit time.
	 */
	private static double[] getInflows(
						String[] soluteNames, EnvironmentContainer environment)
	{
		double[] out = Vector.zerosDbl(soluteNames.length);
		double rate, sIn;
		ChemostatInflow cIn;
		String soluteName;
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
			if ( aBoundary instanceof ChemostatInflow )
			{
				cIn = (ChemostatInflow) aBoundary;
				/* Rate should be in units of 1/time. */
				rate = cIn.getFlowRate();
				for ( int i = 0; i < soluteNames.length; i++ )
				{
					soluteName = soluteNames[i];
					sIn = cIn.getConcentration(soluteName);
					out[i] += rate * sIn;
				}
			}
		return out;
	}
	
	/**
	 * \brief Loop through all agents in a {@code Compartment}, performing
	 * their reactions and storing the result in <b>destination</b>.
	 * 
	 * @param destination {@code double[]} vector of solute production
	 * (positive)/consumption (negative) rates for each solute in
	 * <b>soluteNames</b>.
	 * @param agents The agents of a {@code Compartment}.
	 * @param concns Dictionary of concentrations for solutes.
	 * @param soluteNames List of solute names in the same order as
	 * <b>destination</b>.
	 */
	private static void applyAgentReactions(
							double[] destination, AgentContainer agents,
							Map<String, Double> concns, String[] soluteNames)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			@SuppressWarnings("unchecked")
			List<Reaction> reactions = 
							(List<Reaction>) agent.get("reactions");
			if ( reactions == null )
				continue;
			// TODO get agent biomass concentrations?
			for (Reaction aReac : reactions)
				applyProductionRates(destination, aReac, concns, soluteNames);
			// TODO tell the agent about the rates of production of its
			// biomass types?
		}
	}
	
	/**
	 * \brief Apply the given reaction to a destination vector of production
	 * rates.
	 * 
	 * <p>Note that the rates in the destination vector will be adjusted, not
	 * overwritten. There must be an exact mapping between the destination
	 * vector and the vector of solute names.</p>
	 * 
	 * @param destination {@code double[]} vector of solute production
	 * (positive)/consumption (negative) rates for each solute in
	 * <b>soluteNames</b>.
	 * @param aReac A reaction to apply.
	 * @param concns Dictionary of concentrations for solutes and reactants.
	 * @param soluteNames List of solute names in the same order as
	 * <b>destination</b>.
	 */
	private static void applyProductionRates(double[] destination, 
			Reaction aReac, Map<String, Double> concns, String[] soluteNames)
	{
		double rate = aReac.getRate(concns);
		for ( int i = 0; i < soluteNames.length; i++ )
			destination[i] += rate * aReac.getStoichiometry(soluteNames[i]);
	}
	
	/*************************************************************************
	 * AGENT MIGRATIONS
	 ************************************************************************/
	
	/**
	 * \brief Accept all inbound {@code Agent}s on connecting boundaries, 
	 * simply adding them to the {@code AgentContainer}.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 */
	public static void acceptAllInboundAgents(
			EnvironmentContainer environment, AgentContainer agents)
	{
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
		{
			for ( Agent anAgent : aBoundary.getAllInboundAgents() )
				agents.addAgent(anAgent);
			aBoundary.clearArrivalsLoungue();
		}
	}
	
	/**
	 * \brief Perform the dilution of agents from a chemostat 
	 * {@code Compartment}.
	 * 
	 * @param timeStepSize Size of the time step.
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 */
	public static void diluteAgents(double timeStepSize,
					EnvironmentContainer environment, AgentContainer agents)
	{
		List<ChemostatOutflow> outflows = getOutflows(environment);
		/*
		 * Update the tally of Agents that ought to be diluted through each
		 * connection. 
		 */
		double tally = 0.0;
		for ( ChemostatOutflow cOut : outflows )
		{
			cOut.updateAgentsToDiluteTally(timeStepSize);
			tally += cOut.getAgentsToDiluteTally();
		}
		Log.out(Tier.EXPRESSIVE, "Chemostat contains "+agents.getNumAllAgents()
						+" agents; diluting a maximum of "+tally
						+" through "+outflows.size()+" connections");
		/*
		 * Now keep flushing out agents until all connections are satisfied or
		 * we have no more agents left.
		 */
		Agent anAgent;
		while ( true )
		{
			/*
			 * Check there are outflows that still need to be satisfied.
			 */
			if ( ! areOutflowsToDiluteAgents(outflows) )
				break;
			/*
			 * If there are no more agents left then exit.
			 */
			if ( agents.getNumAllAgents() == 0 )
				break;
			/*
			 * 
			 */
			anAgent = agents.extractRandomAgent();
			getNextOutflow(outflows).addOutboundAgent(anAgent);
		}
	}
	
	/**
	 * \brief Get a list of chemostat connection boundaries where the flow is
	 * out of the {@code Compartment}.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @return A list of chemostat connection boundaries.
	 */
	private static List<ChemostatOutflow> getOutflows(
											EnvironmentContainer environment)
	{
		List<ChemostatOutflow> out = new LinkedList<ChemostatOutflow>();
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
			if ( aBoundary instanceof ChemostatOutflow )
				out.add((ChemostatOutflow) aBoundary);
		return out;
	}
	
	/**
	 * \brief Check whether the given chemostat connections still need to
	 * dilute more agents.
	 * 
	 * @param outflows List of chemostat connection boundaries.
	 * @return Whether or not we need to continue diluting agents.
	 */
	private static boolean areOutflowsToDiluteAgents(
											List<ChemostatOutflow> outflows)
	{
		/*
		 * Only connections with a tally of at least one agent are considered
		 * viable for agent dilution.
		 */
		for ( ChemostatOutflow cOut : outflows )
			if ( cOut.getAgentsToDiluteTally() >= 1.0 )
				return true;
		return false;
	}
	
	/**
	 * \brief Choose a chemostat connection boundary to dilute an agent through.
	 * 
	 * @param outflows List of chemostat connection boundaries.
	 * @return A chemostat connection boundary.
	 */
	private static ChemostatOutflow getNextOutflow(
											List<ChemostatOutflow> outflows)
	{
		/*
		 * We choose the next boundary by a weighted probability: all
		 * viable connections are weighted by their remaining tally. A
		 * connection is deemed viable if its tally is at least one (see 
		 * areOutflowsToDiluteAgents(List<ChemostatOutflow>)).
		 */
		double tallyTotal = 0.0;
		for ( ChemostatOutflow cOut : outflows )
			if ( cOut.getAgentsToDiluteTally() > 1.0 )
				tallyTotal += cOut.getAgentsToDiluteTally();
		tallyTotal *= ExtraMath.getUniRandDbl();
		for ( ChemostatOutflow cOut : outflows )
			if ( cOut.getAgentsToDiluteTally() >= 1.0 )
			{
				tallyTotal -= cOut.getAgentsToDiluteTally();
				if ( tallyTotal < 0.0 )
				{
					cOut.knockDownAgentsToDiluteTally();
					return cOut;
				}
			}
		return null;
	}
}
