package processManager.library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import aspect.AspectRef;
import boundary.Boundary;
import boundary.BoundaryLibrary.ChemostatInflow;
import boundary.BoundaryLibrary.ChemostatOutflow;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import reaction.Reaction;
import solver.ODEderivatives;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import utility.ExtraMath;
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
	protected LinkedList<ChemostatOutflow> _outflows = 
									new LinkedList<ChemostatOutflow>();
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
	
	@Override
	public void init(Element xmlElem, Compartment compartment)
	{
		super.init(xmlElem);
		this.init();
	}
	
	/**
	 * TODO
	 */
	public void init()
	{
		this.init((String[]) reg().getValue(this, SOLUTE_NAMES));
	}

	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		/*
		 * Initialise the solver.
		 */
		// TODO This should be done better
		String solverName = this.getString(SOLVER);
		solverName = Helper.setIfNone(solverName, "rosenbrock");
		double hMax = Helper.setIfNone(this.getDouble(HMAX), 1.0e-6);
		if ( solverName.equals("heun") )
			this._solver = new ODEheunsmethod(soluteNames, false, hMax);
		else
		{
			double tol = Helper.setIfNone(this.getDouble(TOLERANCE), 1.0e-6);
			this._solver = new ODErosenbrock(soluteNames, false, tol, hMax);
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
	protected void internalStep(EnvironmentContainer environment,
			AgentContainer agents)
	{
		Tier level = Tier.DEBUG;
		Log.out(level, "SolveChemostat internal step starting");
		/*
		 * Accept any inbound agents
		 */
		// TODO remove? This should be handled by the Compartment at the start
		// of the global time step
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
			{
				for ( Agent anAgent : aBoundary.getAllInboundAgents() )
					agents.addAgent(anAgent);
				aBoundary.clearArrivalsLoungue();
			}
		/*
		 * Update information that depends on the environment.
		 */
		this.updateDilutionInflow(environment);
		this.updateConcnsAndY(environment);
		/*
		 * Update the solver's derivative functions (dY/dT, dF/dT, dF/dY).
		 */
		this._solver.setDerivatives(this.standardUpdater(environment, agents));
		/*
		 * Solve the system and update the environment.
		 */
		Log.out(level, " About to start solver: y = "+Vector.toString(this._y));
		try { this._y = this._solver.solve(this._y, this._timeStepSize); }
		catch ( Exception e) { e.printStackTrace();}
		Log.out(level, " Solver finished: y = "+Vector.toString(this._y));
		updateEnvironment(environment);
		/*
		 * Finally, select Agents to be washed out of the Compartment.
		 */
		// TODO remove? This should be handled by the Compartment at the end
		// of the global time step
		this.diluteAgents(agents, environment);
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
		double outRate = 0.0;
		this._outflows.clear();
		/*
		 * Loop over all chemostat connections.
		 */
		for ( Boundary aBoundary : environment.getOtherBoundaries() )
		{
			if ( aBoundary instanceof ChemostatInflow )
			{
				ChemostatInflow cIn = (ChemostatInflow) aBoundary;
				for ( int i = 0; i < this.n(); i++ )
				{
					this._dYdTinflow[i] += cIn.getFlowRate() * 
									cIn.getConcentration(this._soluteNames[i]);
				}
			}
			else if ( aBoundary instanceof ChemostatOutflow )
			{
				ChemostatOutflow cOut = (ChemostatOutflow) aBoundary;
				outRate += cOut.getFlowRate();
				this._outflows.add(cOut);
			}
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
												HashMap<String, Double> concns)
	{
		double rate = aReac.getRate(concns);
		double stoichiometry;
		for ( int i = 0; i < this.n(); i++ )
		{
			stoichiometry = aReac.getStoichiometry(this._soluteNames[i]);
			destination[i] += rate * stoichiometry;
		}
	}
	
	/*************************************************************************
	 * AGENT MIGRATIONS
	 ************************************************************************/
	
	// TODO this whole section should be moved into the relevant classes of
	// boundary.agent.AgentMethodLibrary
	
	
	/**
	 * \brief
	 * 
	 * FIXME Currently only deals with flows out of chemostat via a
	 * ChemostatConnection, i.e. into another chemostat.
	 * 
	 * @param agents
	 * @param environment
	 */
	private void diluteAgents(AgentContainer agents, EnvironmentContainer environment)
	{
		/*
		 * Update the tally of Agents that ought to be diluted through each
		 * connection. 
		 */
		double tally = 0.0;
		for ( ChemostatOutflow cOut : this._outflows )
		{
			cOut.updateAgentsToDiluteTally(this._timeStepSize);
			tally += cOut.getAgentsToDiluteTally();
		}
		Log.out(Tier.EXPRESSIVE, "Chemostat contains "+agents.getNumAllAgents()
						+" agents; diluting a maximum of "+tally
						+" through "+this._outflows.size()+" connections");
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
			if ( ! this.areOutflowsToDiluteAgents() )
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
			this.getNextOutflow().addOutboundAgent(anAgent);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean areOutflowsToDiluteAgents()
	{
		for ( ChemostatOutflow cOut : this._outflows )
			if ( cOut.getAgentsToDiluteTally() >= 1.0 )
				return true;
		return false;
	}
	
	private ChemostatOutflow getNextOutflow()
	{
		double tallyTotal = 0.0;
		for ( ChemostatOutflow aChemoConnect : this._outflows )
			if ( aChemoConnect.getAgentsToDiluteTally() > 1.0 )
				tallyTotal += aChemoConnect.getAgentsToDiluteTally();
		tallyTotal *= ExtraMath.getUniRandDbl();
		for ( ChemostatOutflow aChemoConnect : this._outflows )
			if ( aChemoConnect.getAgentsToDiluteTally() >= 1.0 )
			{
				tallyTotal -= aChemoConnect.getAgentsToDiluteTally();
				if ( tallyTotal < 0.0 )
				{
					aChemoConnect.knockDownAgentsToDiluteTally();
					return aChemoConnect;
				}
			}
		return null;
	}
}
