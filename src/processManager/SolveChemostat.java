package processManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import agent.Agent;
import boundary.Boundary;
import boundary.ChemostatConnection;
import dataIO.Log;
import dataIO.Log.tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.ODEderivatives;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class SolveChemostat extends ProcessManager
{
	/**
	 * The ODE solver to use when updating solute concentrations. 
	 */
	protected ODEsolver _solver;

	/**
	 * The names of all solutes this is responsible for.
	 */
	protected String[] _soluteNames;
	
	/**
	 * Vector of inflow rates, in units of concentration per unit time. 
	 */
	protected double[] _dYdTinflow;
	
	/**
	 * Dilution rate in units of time<sup>-1</sup>.
	 */
	protected double _dilution = 0.0;
	
	/**
	 * Temporary dictionary of all solute concentrations in the environment,
	 * <i>not just those handled by this process manager.</i>
	 */
	private HashMap<String, Double> _concns = new HashMap<String, Double>();
	
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
	 * TODO
	 */
	public void init()
	{
		init((String[]) reg().getValue(this, "soluteNames"));
	}

	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		// NOTE Bas: yes, yes I'll make nicer methods for this..
		/* Name of the solver. */
		String solverName = String.valueOf(reg().getValue(this, "solver"));
		solverName = Helper.setIfNone(solverName, "rosenbrock");
		/* hMax. */
		String hMaxStr = String.valueOf(reg().getValue(this, "hMax"));
		double hMax = Helper.getDouble(hMaxStr, 1.0e-6);
		/* Make the solver. */
		if ( solverName.equals("heun") )
			this._solver = new ODEheunsmethod(soluteNames, false, hMax);
		else
		{
			/* Need the tolerance. */
			String tolStr = String.valueOf(reg().getValue(this, "tolerance"));
			double tol = Helper.getDouble(tolStr, 1.0e-6);
			/* Set the solver. */
			this._solver = new ODErosenbrock(soluteNames, false, tol, hMax);
		}
		/*
		 * Initialise vectors that need the number of solutes.
		 */
		this._dYdTinflow = new double[this.n()];
		this._y = new double[this.n()];
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Number of solutes.
	 */
	private int n()
	{
		return this._soluteNames.length;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
			AgentContainer agents)
	{
		/*
		 * Update information that depends on the environment.
		 */
		this.updateDilutionInflow(environment);
		this.updateConcnsAndY(environment);
		/*
		 * Update the solver's derivative functions (dY/dT, dF/dT, dF/dY).
		 */
		ODEderivatives deriv = new ODEderivatives()
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
									(List<Reaction>) agent.get("reactions");
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
		this._solver.setDerivatives(deriv);
		/*
		 * Finally, solve the system and update the environment.
		 */
		try { this._y = this._solver.solve(this._y, this._timeStepSize); }
		catch ( Exception e) { e.printStackTrace();}
		Log.out(tier.DEBUG, "y is now "+Arrays.toString(this._y));
		updateEnvironment(environment);
	}
	
	/**
	 * \brief TODO
	 */
	private void updateDilutionInflow(EnvironmentContainer environment)
	{
		/* Reset counters */
		Vector.reset(this._dYdTinflow);
		double inRate = 0.0, outRate = 0.0;
		/*
		 * Loop over all chemostat connections.
		 */
		double rate, sIn;
		ChemostatConnection aChemoConnect;
		String soluteName;
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
			if ( aBoundary instanceof ChemostatConnection )
			{
				aChemoConnect = (ChemostatConnection) aBoundary;
				rate = aChemoConnect.getFlowRate();
				if ( rate > 0.0 )
				{
					inRate += rate;
					for ( int i = 0; i < this._soluteNames.length; i++ )
					{
						soluteName = this._soluteNames[i];
						sIn = aChemoConnect.getConcentration(soluteName);
						this._dYdTinflow[i] += rate * sIn;
					}
				}
				else
					outRate -= rate;
			}
		/*
		 * If the in- and out-rates don't match then the volume would change.
		 * 
		 * TODO handle this!
		 */
		if ( ! ExtraMath.areEqual(inRate, outRate, 1.0e-10) )
		{
			throw new IllegalArgumentException(
							"Chemostat inflow and outflow rates must match!"+
							" Inflow: "+inRate+" | Outflow: "+outRate);
		}
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
	 * \brief TODO
	 * 
	 * @param aReac
	 * @param concns
	 * @param dYdT
	 */
	private void applyProductionRates(double[] destination, Reaction aReac,
												HashMap<String, Double> concns)
	{
		for ( int i = 0; i < this.n(); i++ )
		{
			destination[i] +=
					aReac.getProductionRate(concns, this._soluteNames[i]);
		}
	}
}
