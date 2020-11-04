package processManager.library;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import bookkeeper.KeeperEntry.EventType;
import boundary.Boundary;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Global;
import linearAlgebra.Vector;
import processManager.ProcessManager;
import processManager.ProcessMethods;
import reaction.Reaction;
import reaction.RegularReaction;
import referenceLibrary.AspectRef;
import solver.ODEderivatives;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import utility.Helper;

/**
 * 
 * Used in combination with ODE solver, passing an ordered double array of 
 * variable moieties, passed as initial values to the ODE solver (y). The
 * ODEderivatives function returned by {@link #standardUpdater(
 * EnvironmentContainer, AgentContainer) standardUpdater} is used to calculated 
 * and return the derivative (dy/dt) in the same order. After the {@link 
 * ODEsolver#solve(double[], double) ODE step} updated values are stored.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class ChemostatSolver extends ProcessManager
{
	public static String SOLUTE_NAMES = AspectRef.soluteNames;
	public static String SOLVER = AspectRef.solver;
	public static String HMAX = AspectRef.solverhMax;
	public static String TOLERANCE = AspectRef.solverTolerance;
	public static String REACTIONS = AspectRef.agentReactions;
	public static String SOLUTES = AspectRef.soluteNames;
	public static String AGENT_VOLUME = AspectRef.agentVolume;
	public static String DIVIDE = AspectRef.agentDivision;

	public static String DISABLE_BULK_DYNAMICS = AspectRef.disableBulkDynamics;
	/**
	 * The ODE solver to use when updating solute concentrations. 
	 */
	protected ODEsolver _solver;
	/**
	 * The names of all solutes this is responsible for.
	 */
	protected String[] _solutes = new String[0];
	/**
	 * number of solutes
	 */
	protected int _n;
	
	protected boolean diableBulk = false;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		String[] soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(
				environment.getSoluteNames()));
		this._solutes = soluteNames;
		this._n = this._solutes == null ? 0 : this._solutes.length;
		if (this.isAspect(DISABLE_BULK_DYNAMICS))
			this.diableBulk = this.getBoolean(DISABLE_BULK_DYNAMICS);
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		/* 
		 * initial values
		 */
		LinkedList<Double> y = new LinkedList<Double>();
		
		double vol = this._environment.getShape().getTotalVolume();
		
		/* solutes */
		for( int i = 0; i < this._n; i++ )
		{
			y.add(i, this._environment.getAverageConcentration( _solutes[i] ) * vol );
		}
		
		/* volume: Store in y vector allows for changing volume */
		y.add( _n , vol );	
		
		/* Agents: obtain mass map and add to the y vector */
		int yAgent = 0;
		for ( Agent a : this._agents.getAllAgents() )
		{
			Map<String,Double> agentMap = ProcessMethods.getAgentMassMap( a );
			int i = 0;
			for ( String s : agentMap.keySet() )
			{
				i++;
				/* mass and internal products */
				y.add( _n + yAgent + i, agentMap.get( s ) );
			}
			yAgent += agentMap.size();	
		}
		
		/* set the solver */
		double[] yODE = Vector.vector( y );
		this.setSolver( yODE.length );
		
		/*
		 * Solve the odes
		 */
		try {
			yODE = this._solver.solve( yODE , this._timeStepSize);
		} catch (Exception e) {
			Log.out(Tier.CRITICAL, "Error in ODE solver: " + e);
			e.printStackTrace();
		}
		
		/* register changes to bookkeeper */
		if( Global.bookkeeping )
			for ( int i = 0; i < this._n; i++ )
				this._agents._compartment.registerBook(
						EventType.ODE, 
						this._solutes[i], 
						null, 
						String.valueOf(yODE[i]-y.get(i)), null);
		
		/* convert solute mass rate to concentration rate to 
		 * concentration rates 
		 * 
		 * NOTE this description says rate though we are looking at y not dydt
		 * thus for now I assume the description is wrong, check and update
		 * description. */
		for ( int i = 0; i < _n; i++ )
		{
			yODE[i] /= yODE[ _n ];
		}

		/*
		 * Update the environment
		 */
		if( !this.diableBulk )
		{
			for ( int i = 0; i < this._n; i++ )
				this._environment.setAllConcentration( this._solutes[i], yODE[i]);
			this._environment.getShape().setTotalVolume( yODE[ _n ] );
			if( Log.shouldWrite(Tier.DEBUG) )
				Log.out(Tier.DEBUG, "new volume: " + yODE[ _n ] );
			}
		
		/* 
		 * Update the agents 
		 */
		yAgent = 0;
		for ( Agent a : this._agents.getAllAgents() )
		{
			Map<String,Double> agentMap = ProcessMethods.getAgentMassMap( a );			
			int i = 0;
			for ( String s : agentMap.keySet() )
			{
				i++;
				/* mass and internal products */
				agentMap.put( s, yODE[ _n + yAgent + i ] );
			}
			yAgent += agentMap.size();
			ProcessMethods.updateAgentMass(a, agentMap );
		}
		
		/* perform final clean-up and update agents to represent updated 
		 * situation. */
		this.postStep();
	}
	
	/* ***********************************************************************
	 * INTERNAL METHODS
	 * **********************************************************************/
	
	/**
	 * \brief: setup ode solver with settings from protocol file
	 * @param l: length of array y (number of variables to solve for)
	 */
	public void setSolver( int l )
	{
		/*
		 * Initialize the solver, note we are doing this here because the length
		 * of y depends on the number of agents
		 */
		String solverName = (String) this.getOr(SOLVER, "heun");
		double hMax = (double) this.getOr(HMAX, 1.0e-6);
		if ( solverName.equals("rosenbrock") )
		{
			double tol = (double) this.getOr(TOLERANCE, 1.0e-6);
			this._solver = new ODErosenbrock( new String[l], false, tol, hMax);
		}
		else
			this._solver = new ODEheunsmethod( new String[l], false, hMax);
		this._solver.setDerivatives( this.standardUpdater( this._environment, 
				this._agents));
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
			public void firstDeriv(double[] dydt, double[] y)
			{
				/*
				 * Setup and re-used objects
				 */
				Vector.setAll(dydt, 0.0);
				HashMap<String, Double> soluteMap = 
						new HashMap<String, Double>();
				for( int i = 0; i < _n; i++ )
					soluteMap.put( _solutes[i], y[i]/y[_n] );
				
				/*
				 * In and out flows
				 */
				for ( Boundary aBoundary : environment.getNonSpatialBoundaries() )
				{
					double volFlowRate = aBoundary.getVolumeFlowRate();
					if ( volFlowRate < 0.0 )
					{
						/* outflows at bulk concentrations */
						dydt [_n ] += volFlowRate;
						for ( int i = 0; i < _n; i++ )
							dydt[i] += volFlowRate * (y[i]/y[_n]);
					}
					else if ( volFlowRate > 0.0 )
					{
						/* inflows determined by boundary */
						dydt [_n ] += volFlowRate;
						for ( int i = 0; i < _n; i++ )
							dydt[i] += aBoundary.getMassFlowRate( _solutes[i] );
					}
					else
					{
						/* diffusion flows */
						dydt [_n ] += volFlowRate;
						for ( int i = 0; i < _n; i++ )
							dydt[i] += aBoundary.getMassFlowRate( _solutes[i] );
					}
					if ( volFlowRate != 0.0 )
					{
						if( Log.shouldWrite(Tier.DEBUG))
							Log.out(Tier.DEBUG, "volume change: " + volFlowRate 
									+ "temp net change: " + dydt [_n]);
					}
				}
				/*
				 * Environment reactions
				 */
				for ( Reaction aReac : environment.getReactions() )
				{
					for ( int i = 0; i < _n; i++ )
					{
						dydt[i] += aReac.getProductionRate( soluteMap, 
								_solutes[i]);
					}
				}
				/*
				 * Agents
				 */
				int yAgent = 0;
				for ( Agent a : agents.getAllAgents() )
				{
					@SuppressWarnings("unchecked")
					List<RegularReaction> reactions = 
							(List<RegularReaction>) a.get(REACTIONS);
					if ( reactions == null )
						return;

					/* obtain massMap */
					Map<String,Double> agentMap = 
							ProcessMethods.getAgentMassMap( a );
					
					/* use up to date values for solvers */
					int j = 0;
					for( String s : agentMap.keySet())
					{
						j++;
						agentMap.put(s, y[ _n + yAgent + j ]);
					}
					
					/* create reactionMap and add current concentrations */
					Map<String,Double> reactionMap = 
							new HashMap<String, Double>();
					reactionMap.putAll( soluteMap );

					for (Reaction aReac : reactions)
					{
						/* Add any constants from the aspects */
						j = 0;
						for ( String var : aReac.getConstituentNames() )
							if ( !agentMap.containsKey( var ) )
							{
								if ( a.isAspect( var ) )
									reactionMap.put( var, a.getDouble( var) );
								else if ( ! soluteMap.containsKey( var ) )
									reactionMap.put(var , 0.0);
							}
						
						reactionMap.putAll( agentMap );						
						/*
						 * Apply the effect of this reaction on the relevant 
						 * solutes and agent constituents.
						 */
						for ( int i = 0; i < _n; i++ )
						{
							dydt[i] += aReac.getProductionRate( reactionMap, 
									_solutes[i] );
						}
						/*
						 * Apply the effect of this reaction to the agent and
						 * its internal moieties.
						 */
						int i = 0;
						for ( String s : agentMap.keySet() )
						{
							if ( !soluteMap.containsKey(s) )
							{
								i++;
								/* mass and internal products */
								dydt[ _n + yAgent + i ] += 
										aReac.getProductionRate(reactionMap, s);
							}
						}
					}
					yAgent += agentMap.size();
				}
			}
		};
	}
	
	protected void postStep()
	{
		/*
		 * Ask all boundaries to update their solute concentrations.
		 */
		this._environment.updateSoluteBoundaries();
		/**
		 * act upon new agent situations
		 */
		for(Agent agent: this._agents.getAllAgents()) 
		{
			agent.event(DIVIDE);
		}
	}
}