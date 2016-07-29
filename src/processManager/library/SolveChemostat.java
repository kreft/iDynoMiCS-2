package processManager.library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.Boundary;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import solver.ODEderivatives;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import utility.Helper;

/**
 * \brief Controls the solute dynamics of a {@code Compartment} with no
 * spatial structure (i.e., a {@code Dimensionless} shape).
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
// TODO this manager may need to update the solute concentrations of 
// out-flowing connection boundaries.
public class SolveChemostat extends ProcessManager
{
	
	public static String SOLUTE_NAMES = AspectRef.soluteNames;
	public static String SOLVER = AspectRef.solver;
	public static String HMAX = AspectRef.solverhMax;
	public static String TOLERANCE = AspectRef.solverTolerance;
	public static String REACTIONS = AspectRef.agentReactions;
	public String SOLUTES = AspectRef.soluteNames;
	
	/**
	 * The ODE solver to use when updating solute concentrations. 
	 */
	protected ODEsolver _solver;
	/**
	 * The names of all solutes this is responsible for.
	 */
	protected String[] _soluteNames = new String[0];
	/**
	 * Vector of inflow rates, in units of concentration per unit time.
	 */
	protected double[] _dYdTinflow;
	/**
	 * Dilution rate in units of time<sup>-1</sup>.
	 */
	protected double _dilution = 0.0;
	/**
	 * TODO
	 */
	// TODO delete?
	protected List<Boundary> _outflows = new LinkedList<Boundary>();
	/**
	 * Temporary dictionary of all solute concentrations in the environment,
	 * <i>not just those handled by this process manager.</i>
	 */
	private Map<String, Double> _concns = new HashMap<String, Double>();
	/**
	 * Temporary vector of solute concentrations in the same order as
	 * _soluteNames.
	 */
	private double[] _y;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this.init(environment, agents, compartmentName);
	}
	
	@Override
	public void init(EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		String[] soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(
				environment.getSoluteNames()));

		this.init(soluteNames, environment, 
				agents, compartmentName);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 * @param environment
	 * @param agents
	 * @param compartmentName
	 */
	public void init(String[] soluteNames, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		/* This super call is only required for the unit tests. */
		super.init(environment, agents, compartmentName);
		this._soluteNames = soluteNames;
		/*
		 * Initialise the solver.
		 */
		String solverName = (String) this.getOr(SOLVER, "rosenbrock");
		double hMax = (double) this.getOr(HMAX, 1.0e-6);
		if ( solverName.equals("heun") )
			this._solver = new ODEheunsmethod(_soluteNames, false, hMax);
		else
		{
			double tol = (double) this.getOr(TOLERANCE, 1.0e-6);
			this._solver = new ODErosenbrock(_soluteNames, false, tol, hMax);
		}
		/*
		 * Initialise vectors that need the number of solutes.
		 */
		this._dYdTinflow = new double[this.n()];
		this._y = new double[this.n()];
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		Tier level = Tier.DEBUG;
		Log.out(level, "SolveChemostat internal step starting");
		/*
		 * Update information that depends on the environment.
		 */
		this.updateFlowRates();
		this.updateConcnsAndY();
		/*
		 * Update the solver's derivative functions (dY/dT, dF/dT, dF/dY).
		 */
		this._solver.setDerivatives(
				this.standardUpdater(this._environment, this._agents));
		/*
		 * Solve the system and update the environment.
		 */
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, 
					" About to start solver: y = "+Vector.toString(this._y));
		}
		try { this._y = this._solver.solve(this._y, this._timeStepSize); }
		catch ( Exception e) { e.printStackTrace();}
		if ( Log.shouldWrite(level) )
			Log.out(level, " Solver finished: y = "+Vector.toString(this._y));
		this.updateEnvironment();
		/*
		 * Finally, select Agents to be washed out of the Compartment.
		 */
		Log.out(level, "SolveChemostat internal step finished");
	}
	
	/* ***********************************************************************
	 * INTERNAL METHODS
	 * **********************************************************************/
	
	/**
	 * \brief Number of solutes this process manager deals with.
	 */
	private int n()
	{
		return this._soluteNames == null ? 0 : this._soluteNames.length;
	}
	
	/**
	 * \brief The standard set of Ordinary Differential Equations (ODEs) that
	 * describe a chemostat compartment.
	 * 
	 * <p>This is also appropriate for, e.g., a batch culture compartment: the
	 * absence of inflows & outflows means that these are implicitly ignored
	 * here. </p>
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 * @return ODE derivatives method.
	 */
	private ODEderivatives standardUpdater(
			EnvironmentContainer environment, AgentContainer agents)
	{
		return new ODEderivatives()
		{
			@Override
			public void firstDeriv(double[] destination, double[] y)
			{
				/*
				 * First deal with inflow and dilution: dSdT = D(Sin - S)
				 */
				Vector.timesTo(destination, y, - _dilution);
				Vector.addEquals(destination, _dYdTinflow);
				/*
				 * For the reactions, we will need the concentrations in
				 * dictionary format.
				 */
				updateConcns(y);
				/*
				 * Apply agent reactions to the vector of first derivatives.
				 * Note that any agents without reactions will return null, and
				 * so will be skipped.
				 */
				for ( Agent agent : agents.getAllAgents() )
					applyAgentReactions(destination, agent, _concns);
				/*
				 * Apply extracellular reactions to the vector of first
				 * derivatives.
				 */
				for ( Reaction aReac : environment.getReactions() )
					applyProductionRates(destination, aReac, _concns);
			}
			
			@Override
			public void postMiniStep(double[] y, double dt)
			{
				String name;
				double volRate, massRate;
				for ( Boundary aBoundary : _outflows )
				{
					volRate = aBoundary.getVolumeFlowRate();
					for ( int i = 0; i < n(); i++ )
					{
						name = _soluteNames[i];
						// TODO Check if we need to apply volume-change
						// conversions here
						massRate = volRate * y[i];
						aBoundary.increaseMassFlowRate(name, massRate);
					}
				}
				/*
				 * Loop through all agents, applying their reactions to
				 * themselves. For the reactions, we will need the
				 * solute concentrations in dictionary format.
				 */
				updateConcns(y);
				for ( Agent agent : _agents.getAllAgents() )
					applyAgentReactions(agent, _concns, dt);
			}
		};
	}
	
	/**
	 * \brief Update the private {@link #_concns} dictionary of solute
	 * concentrations.
	 * 
	 * @param y Vector of solute concentrations from the ODE solver.
	 */
	private void updateConcns(double[] y)
	{
		this._concns = this._environment.getAverageConcentrations();
		for ( int i = 0; i < n(); i++ )
			this._concns.put(this._soluteNames[i], y[i]);
	}
	
	/**
	 * \brief Ask the compartment's boundaries for information about the
	 * flow of media in and out.
	 */
	private void updateFlowRates()
	{
		Tier level = Tier.DEBUG;
		/* Reset counters */
		Vector.reset(this._dYdTinflow);
		double inRate = 0.0, outRate = 0.0;
		this._outflows.clear();
		/*
		 * Loop over all boundaries, asking for their flow rates.
		 */
		for ( Boundary aBoundary : this._environment.getOtherBoundaries() )
		{
			double volFlowRate = aBoundary.getVolumeFlowRate();
			if ( volFlowRate < 0.0 )
			{
				/*
				 * This is an outflow, so we calculate the mass flow rates as
				 * the solver runs.
				 */
				outRate -= volFlowRate;
				this._outflows.add(aBoundary);
			}
			else
			{
				/*
				 * This is either an inflow or a no-flow. In either case, use
				 * the mass flow rates given by the boundary.
				 */
				inRate += volFlowRate;
				for ( int i = 0; i < this.n(); i++ )
				{
					this._dYdTinflow[i] += 
							aBoundary.getMassFlowRate(this._soluteNames[i]);
				}
			}
		}
		/*
		 * If the in- and out-rates don't match then the volume would change.
		 * 
		 * TODO handle this!
		 */
		/*
		if ( Math.abs(totalVolRate) > 1.0e-10 )
		{
			throw new IllegalArgumentException(
							"Chemostat inflow and outflow rates must match!");
		}
		*/
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "Chemostat: total inflows "+inRate+
					", total outflows "+outRate);
		}
		this._dilution = outRate;
	}
	
	/**
	 * \brief Update the private _concn and _y variables with average solute
	 * concentrations from the environment.
	 */
	private void updateConcnsAndY()
	{
		double concn;
		String name;
		for ( int i = 0; i < this.n(); i++ )
		{
			name = this._soluteNames[i];
			concn = this._environment.getAverageConcentration(name);
			this._concns.put(name, concn);
			this._y[i] = concn;
		}
	}

	/**
	 * \brief Push all new values of solute concentrations to the relevant
	 * grids in the environment.
	 */
	private void updateEnvironment()
	{
		for ( int i = 0; i < this.n(); i++ )
		{
			this._environment.setAllConcentration(
					this._soluteNames[i], this._y[i]);
		}
	}
	
	/**
	 * @return The compartment volume.
	 */
	private double volume()
	{
		return Math.pow(this._agents.getShape().getTotalVolume(), -1.0);
	}
	
	/**
	 * \brief Loop through an agent's reactions, applying the result to a
	 * destination vector given.
	 * 
	 * @param destination Vector describing the production/consumption
	 * rates of environmental solutes (overwritten).
	 * @param agent Agent assumed to have reactions (unaffected by this method).
	 * @param concns Concentrations of solutes in the environment (unaffected
	 * by this method).
	 */
	private void applyAgentReactions(double[] destination, Agent agent,
			Map<String, Double> concns)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		@SuppressWarnings("unchecked")
		List<Reaction> reactions = (List<Reaction>) agent.get(REACTIONS);
		if ( reactions == null )
			return;
		/*
		 * Copy the solute concentrations to a new map so that we can add the
		 * agent's mass without risk of these contaminating other agent-based
		 * reactions. Note that multiplication is computationally cheaper than
		 * division, so we calculate perVolume just once.
		 */
		double perVolume = Math.pow(this.volume(), -1.0);
		Map<String,Double> allConcns = AgentContainer.getAgentMassMap(agent);
		for ( String key : allConcns.keySet() )
			allConcns.put(key, allConcns.get(key) * perVolume);
		/*
		 * Copy the solute concentrations to this map so that we do not risk
		 * contaminating other agent-based reactions.
		 */
		allConcns.putAll(concns);
		/*
		 * Loop over all reactions and apply them.
		 */
		double rate, stoichiometry;
		for (Reaction aReac : reactions)
		{
			/*
			 * Check first that we have all variables we need. If not, they may
			 * be stored as other aspects of the agent (e.g. EPS).
			 */
			for ( String varName : aReac.getVariableNames() )
				if ( ! allConcns.containsKey(varName) )
				{
					if ( agent.isAspect(varName) )
					{
						allConcns.put(varName, 
								agent.getDouble(varName) * perVolume);
					}
					else
					{
						// TODO safety?
						allConcns.put(varName, 0.0);
					}
				}
			rate = aReac.getRate(allConcns);
			/*
			 * Apply the effect of this reaction on the relevant solutes.
			 */
			for ( int i = 0; i < this.n(); i++ )
			{
				stoichiometry = aReac.getStoichiometry(this._soluteNames[i]);
				destination[i] += rate * stoichiometry;
			}
		}
	}
	
	/**
	 * \brief Loop through an agent's reactions, applying the result to the
	 * agent's biomass.
	 * 
	 * @param agent Agent assumed to have reactions (biomass will be altered by
	 * this method).
	 * @param concns Concentrations of solutes in the environment (unaffected
	 * by this method).
	 * @param timeStep Length of the mini time-step to use.
	 */
	@SuppressWarnings("unchecked")
	private void applyAgentReactions(Agent agent, 
			Map<String, Double> concns, double timeStep)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		List<Reaction> reactions = (List<Reaction>) agent.get(REACTIONS);
		if ( reactions == null )
			return;
		/*
		 * Get the agent biomass kinds as a map. This map will be the one
		 * updated with the results of the reactions.
		 */
		Map<String,Double> newBiomass = AgentContainer.getAgentMassMap(agent);
		/*
		 * Make a new map with these converted to concentrations. Calculate the
		 * one over volume part once, as multiplication is 
		 */
		double volume = this.volume();
		double perVolume = Math.pow(volume, -1.0);
		Map<String,Double> allConcns = new HashMap<String,Double>();
		for ( String key : newBiomass.keySet() )
			allConcns.put(key, newBiomass.get(key) * perVolume);
		/*
		 * Copy the solute concentrations to this map so that we do not risk
		 * contaminating other agent-based reactions.
		 */
		allConcns.putAll(concns);
		/*
		 * Loop over all reactions and apply them.
		 */
		double reactionOccurances, biomass;
		Map<String,Double> stoichiometry;
		for ( Reaction aReac : reactions )
		{
			stoichiometry = aReac.getStoichiometry();
			reactionOccurances = aReac.getRate(allConcns) * timeStep * volume;
			for ( String key : stoichiometry.keySet() )
				if ( this._environment.isSoluteName(key) )
				{
					/* Do nothing here. */
				}
				else if ( newBiomass.containsKey(key) )
				{
					biomass = newBiomass.get(key) + 
							(stoichiometry.get(key) * reactionOccurances);
					newBiomass.put(key, biomass);
				}
				else
				{
					newBiomass.put(key, 
							stoichiometry.get(key) * reactionOccurances);
				}
		}
		/*
		 * Finally, update the biomass for this agent.
		 */
		AgentContainer.updateAgentMass(agent, newBiomass);
	}
	
	/**
	 * \brief Apply the effects of a reaction on the given vector.
	 * 
	 * @param destination Vector of production rates for the solutes whose
	 * names are stored in {@link #_soluteNames}.
	 * @param aReac The reaction object to apply.
	 * @param concns Dictionary of reactant concentrations.
	 */
	private void applyProductionRates(double[] destination, Reaction aReac,
												Map<String, Double> concns)
	{
		double rate = aReac.getRate(concns);
		double stoichiometry;
		for ( int i = 0; i < this.n(); i++ )
		{
			stoichiometry = aReac.getStoichiometry(this._soluteNames[i]);
			destination[i] += rate * stoichiometry;
		}
	}
}
