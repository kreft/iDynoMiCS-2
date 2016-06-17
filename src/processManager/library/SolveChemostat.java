package processManager.library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import aspect.AspectRef;
import boundary.Boundary;
import boundary.library.ChemostatToChemostat;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import reaction.Reaction;
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
//TODO This manager needs to incorporate chemostat-biofilm interactions,
// i.e. solute flows and agent migrations.
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
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	/**
	 * \brief TODO
	 *
	 */
	public SolveChemostat()
	{

	}
	
	/**
	 * 
	 */
	public void init(Element xmlElem, 
			EnvironmentContainer environment, AgentContainer agents)
	{
		super.init(xmlElem, environment, agents);
		this.init();
	}
	
	/**
	 * \brief TODO
	 * 
	 */
	public void init()
	{
		String[] soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(
				this._compartment.environment.getSoluteNames()));

		init(soluteNames);
	}
	
	public void init(String [] soluteNames)
	{
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
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep()
	{
		Tier level = Tier.DEBUG;
		Log.out(level, "SolveChemostat internal step starting");
		/*
		 * Accept any inbound agents
		 */
		// TODO remove? This should be handled by the Compartment at the start
		// of the global time step
		for ( Boundary aBoundary : this._environment.getOtherBoundaries() )
			{
				for ( Agent anAgent : aBoundary.getAllInboundAgents() )
					this._agents.addAgent(anAgent);
				aBoundary.clearArrivalsLoungue();
			}
		/*
		 * Update information that depends on the environment.
		 */
		this.updateDilutionInflow(this._environment);
		this.updateConcnsAndY(this._environment);
		/*
		 * Update the solver's derivative functions (dY/dT, dF/dT, dF/dY).
		 */
		this._solver.setDerivatives(
				this.standardUpdater(this._environment, this._agents));
		/*
		 * Solve the system and update the environment.
		 */
		Log.out(level, " About to start solver: y = "+Vector.toString(this._y));
		try { this._y = this._solver.solve(this._y, this._timeStepSize); }
		catch ( Exception e) { e.printStackTrace();}
		Log.out(level, " Solver finished: y = "+Vector.toString(this._y));
		updateEnvironment(this._environment);
		/*
		 * Finally, select Agents to be washed out of the Compartment.
		 */
		Log.out(level, "SolveChemostat internal step finished");
	}
	
	/*************************************************************************
	 * INTERNAL METHODS
	 ************************************************************************/
	
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
				_concns = environment.getAverageConcentrations();
				for ( int i = 0; i < n(); i++ )
					_concns.put(_soluteNames[i], y[i]);
				/*
				 * Apply agent reactions. Note that any agents without
				 * reactions will return null, and so will be skipped.
				 */
				for ( Agent agent : agents.getAllAgents() )
				{
					@SuppressWarnings("unchecked")
					List<Reaction> reactions = 
									(List<Reaction>) agent.get(REACTIONS);
					if ( reactions == null )
						continue;
					// TODO get agent biomass concentrations?
					for (Reaction aReac : reactions)
						applyProductionRates(destination, aReac, _concns);
					// TODO tell the agent about the rates of production of its
					// biomass types?
				}
				/*
				 * Apply extracellular reactions.
				 */
				for ( Reaction aReac : environment.getReactions() )
					applyProductionRates(destination, aReac, _concns);
			}
		};
	}
	
	/**
	 * \brief Ask the compartment's boundaries for information about the
	 * flow of media in and out.
	 */
	private void updateDilutionInflow(EnvironmentContainer environment)
	{
		/* Reset counters */
		Vector.reset(this._dYdTinflow);
		double inRate = 0.0, outRate = 0.0;
		this._outflows.clear();
		/*
		 * Loop over all chemostat connections.
		 */
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
		{
			if ( aBoundary instanceof ChemostatToChemostat )
			{
				ChemostatToChemostat c2c = (ChemostatToChemostat) aBoundary;
				double flow = c2c.getFlowRate();
				if ( flow > 0.0 )
				{
					inRate += flow;
					for ( int i = 0; i < this.n(); i++ )
					{
						this._dYdTinflow[i] += flow * 
								c2c.getConcentration(this._soluteNames[i]);
					}
				}
				else
				{
					outRate -= flow;
					this._outflows.add(c2c);
				}
			}
			// TODO ChemostatToBiofilm
		}
		/*
		 * If the in- and out-rates don't match then the volume would change.
		 * 
		 * TODO handle this!
		 */
		/*
		if ( ! ExtraMath.areEqual(inRate, outRate, 1.0e-10) )
		{
			throw new IllegalArgumentException(
							"Chemostat inflow and outflow rates must match!"+
							" Inflow: "+inRate+" | Outflow: "+outRate);
		}
		*/
		Log.out(Tier.DEBUG, "Chemostat: total inflows "+inRate+
				", total outflows "+outRate);
		this._dilution = outRate;
	}
	
	/**
	 * \brief Update the private _concn and _y variables with average solute
	 * concentrations from the environment.
	 * 
	 * @param environment Holder of the current solute concentrations.
	 */
	private void updateConcnsAndY(EnvironmentContainer environment)
	{
		double concn;
		String name;
		for ( int i = 0; i < this.n(); i++ )
		{
			name = this._soluteNames[i];
			concn = environment.getAverageConcentration(name);
			this._concns.put(name, concn);
			this._y[i] = concn;
		}
	}

	/**
	 * \brief Push all new values of solute concentrations to the relevant
	 * grids in the given {@code EnvironmentContainer}.
	 * 
	 * @param environment The destination for the new solute concentrations.
	 */
	private void updateEnvironment(EnvironmentContainer environment)
	{
		for ( int i = 0; i < this.n(); i++ )
			environment.setAllConcentration(this._soluteNames[i], this._y[i]);
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
