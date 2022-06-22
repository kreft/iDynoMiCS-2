/**
 * \package diffusionSolver
 * \brief Package of classes used to capture the diffusion solvers that can be
 * defined in the protocol file
 * 
 * Solvers are used to compute the solute profile within the computational
 * domains. This package is part of iDynoMiCS v1.2, governed by the CeCILL 
 * license under French law and abides by the rules of distribution of free
 * software. You can use, modify and/ or redistribute iDynoMiCS under the
 * terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the
 * following URL  "http://www.cecill.info".
 */
package solver.mgFas;

import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.Log;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Idynomics;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import processManager.ProcessDiffusion;
import processManager.library.PDEWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre
 * for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of
 * Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 */
public class Multigrid
{
	/**
	 * A name assigned to this solver. Specified in the XML protocol file.
	 */
	public String	solverName;

	/**
	 * The position of this solver in the simulation dictionary.
	 */
	public int	solverIndex;

	/**
	 * The computational domain that this solver is associated with. Specified
	 * in the XML protocol file.
	 */
	public Domain myDomain;

	/**
	 * Local copy of the array of solute grids - one for each solute specified
	 * in the simulation protocol file. Taken from simulator object.
	 */
	protected LinkedList<SoluteGrid> _soluteList;

	/**
	 * List of solutes that are used by THIS solver.
	 */
	protected ArrayList<Integer> _soluteIndex = new ArrayList<Integer>();

	protected Double	internTimeStep;

	protected Double	minimalTimeStep;

	protected int	internalIteration;

	/**
	 * Boolean flag that determines whether this solver will actually be used.
	 * Specified in XML protocol file.
	 */
	protected Boolean			_active = false;

	protected MultigridSolute _bLayer;
	
	protected MultigridSolute	_diffusivity;
	protected MultigridSolute[] _solute;
	
//	protected MultigridSolute[]	_biomass;

	protected SoluteGrid[]      allSolute;
	
	protected SoluteGrid[]		allReac;
	
	protected SoluteGrid[]		allDiffReac;

	protected static int        iSolute;
	
	protected static int		order;
	protected int               maxOrder;
	
	/**
	 * Number of solutes SOLVED by THIS solver
	 */
	protected int nSolute;
	
	private HashMap<String, Boolean> _relaxationMap = new HashMap<String, Boolean>();

	/**
	 * 
	 */
//	protected int				nReaction;
	
	/**
	 * Number of times to relax the coarsest grid. Set in the protocol file. 
	 */
	protected int nCoarseStep;
	
	/**
	 * Number of V-cycles to perform during each time step. Set in the
	 * protocol file.
	 */
	protected int _vCycles;
	
	/**
	 * Number of times to relax each multigrid during the downward stroke of a
	 * V-cycle. Set in the protocol file.
	 */
	protected int nPreSteps;
	
	/**
	 * Number of times to relax each multigrid during the upward stroke of a
	 * V-cycle. Set in the protocol file.
	 */
	protected int nPostSteps;

	/**
	 * Set Vcycle convergence mode to reporting only or auto adjusting
	 */
	private boolean autoVcycleAdjust = false;

	protected ProcessDiffusion _manager;

	protected EnvironmentContainer _environment;
	
	protected LinkedList<RecordKeeper> _recordKeepers;

	private boolean initialGuess = false;
	/**
	 * 
	 */
	public void init(Domain domain, EnvironmentContainer environment,
					 AgentContainer agents, PDEWrapper manager,
					 int vCycles, int preSteps, int coarseSteps, int postSteps, boolean autoAdjust)
	{
		/* Get the computational domain that this solver is associated with. */
		myDomain = domain;

		this._manager = manager;
		
		this._recordKeepers = manager.getRecordKeepers();

		this._environment = environment;

		/* Reference all the solutes declared in this system. */
		_soluteList = new LinkedList<SoluteGrid>();

		/* TODO paradigm for if we only want to solve for a subset of solutes
		*  TODO did iDyno 1 store other things in the solute list? It seems
		*   that there are multiple lists all representing solutes..
		*/
		for ( SpatialGrid s : environment.getSolutes() )
			_soluteList.add( new SoluteGrid(domain, s.getName(), s, null ));

		/* Now for each solver, reactions are specified. Add these reactions
		 * and list the solutes that these modify.
		 */
		
		nCoarseStep = coarseSteps;
		_vCycles = vCycles;
		nPreSteps = preSteps;
		nPostSteps = postSteps;

		this.autoVcycleAdjust = autoAdjust;

		// Create the table of solute grids
		nSolute = _soluteList.size();
		_solute = new MultigridSolute[nSolute];
		allSolute = new SoluteGrid[nSolute];
		allReac = new SoluteGrid[nSolute];
		allDiffReac = new SoluteGrid[nSolute];

		/* TODO: here both bLayer and diffusivity initiate from the same grid like in iDyno 1
		*  but both construct a new conc-grid. */
		_bLayer = new MultigridSolute(_soluteList.get(0), "boundary layer", manager);
		_diffusivity = 
				new MultigridSolute(_soluteList.get(0), "relative diffusivity", manager);
		
		Double sBulk;
		for (int i = 0; i < nSolute; i++)
		{
			/* FIXME taking initial as fixed boundary concentration bulk */
				sBulk = environment.getAverageConcentration(_soluteList.get(i).gridName); // this is what the conc grid is set when first solved
				_solute[i] = new MultigridSolute(_soluteList.get(i),
												_diffusivity, _bLayer, sBulk, manager);
				_soluteIndex.add(i); //TODO figure out how solute index was used, adding it here to help program run
		}

		for (int iSolute : _soluteIndex)
		{
			this._relaxationMap.put(_solute[iSolute].soluteName, false);
		}

		/* From this moment, nSolute is the number of solutes SOLVED by THIS
		 * solver.
		 *
		 * TODO aha! this is actually useful so we may set different tolerances
		 *  for low vs high concentration solutes, implement..
		 */

		nSolute = _soluteIndex.size();
//		nReaction = _reactions.size();
		maxOrder = _solute[ _soluteIndex.get(0) ]._conc.length;
		for (RecordKeeper r : this._recordKeepers)
		{
			String soluteName = r.getSoluteName();
			for (int i = 0; i < _solute.length; i++)
			{
				if (_solute[i].soluteName.contentEquals(soluteName))
				{
					Integer order = r.getOrder();
					_solute[i]._conc[order].setRecordKeeper(r);
				}
			}
		}
	}

	/**
	 * \brief Create the solver, initialise the concentration fields, and
	 * solve the diffusion reaction equations.
	 */
	public void initAndSolve()
	{
		initializeConcentrationFields();
		solveDiffusionReaction();
	}

	public void initializeConcentrationFields()
	{
		minimalTimeStep = 0.1*Idynomics.simulator.timer.getTimeStepSize();

		// Refresh, then insert, the boundary layer and the diffusivity grid.
		// NOTE not using Biofilm grids
		myDomain.refreshBioFilmGrids();

		// TODO this is the region in which diffusion is solved?
		_bLayer.setFinest( myDomain.getBoundaryLayer() );
		_bLayer.restrictToCoarsest();

		// TODO this should be per solute in the future?
		_diffusivity.setFinest(myDomain.getDiffusivity());
		_diffusivity.restrictToCoarsest();

		/* TODO we don't need to prepare anything here for the idyno 2
		 *  implementation do we? */
		// Prepare a soluteGrid with catalyst CONCENTRATION.
//		for (int i = 0; i<_biomass.length; i++)
//		{
//			_biomass[i].resetFinest(0.0);
//			_reactions.get(i).fitAgentMassOnGrid(_biomass[i].getFinest());
//			_biomass[i].restrictToCoarsest();
//		}

		for (int iSolute : _soluteIndex)
			_solute[iSolute].readBulk();
	}

	/**
	 * Solve by iterative relaxation.
	 */
	public void solveDiffusionReaction()
	{
		Double timeToSolve = Idynomics.simulator.timer.getTimeStepSize();
		internalIteration = 0;
		internTimeStep = timeToSolve;
		
		/* bvm note 13.7.09:
		 * This iterative loop is only passed through once because of the
		 * value of internTimeStep used above; we leave the loop as-is though
		 * to allow future use of iterates if needed.
		 *
		while ( timeToSolve > 0 ) {
		 */

			// Update bulk concentration.
			updateBulk();

			// Compute new equilibrium concentrations.
			stepSolveDiffusionReaction();

			// Manage iterations.
			internalIteration += 1;
			timeToSolve -= internTimeStep;
//		}

		// Apply results on solute grids
		for (int iSolute : _soluteIndex)
			_solute[iSolute].applyComputation();

		/* flash current concentration to iDyno 2 concentration grids */
		for(int iSolute: _soluteIndex)
		{
			double[][][] out = MultigridUtils.removePadding(
					_solute[iSolute].realGrid.grid );
			this._environment.getSoluteGrid( this._solute[iSolute].soluteName ).
					setTo(ArrayType.CONCN, out );
		}
	}

	/**
	 * One step of the solver
	 */
	public void stepSolveDiffusionReaction()
	{
		for (int iSolute : _soluteIndex)
			_solute[iSolute].resetMultigridCopies();

		int smoothingScalar = 1; // before 4
		boolean breakVCycle = false;
		int vc =0;

		/*
		The outer loop is getting finer (starting from coarsest + 1
		The inner loop is getting coarser and then finer

		        /\    / Finest
		   /\  /  \  /
		/\/  \/    \/   Coarsest


		 order / outer representing finer and coarse grid for
		 */

		int startOrder;
		if( initialGuess ) {
			startOrder = maxOrder - 1;
		} else {
			startOrder = 1;
			solveCoarsest( smoothingScalar ); // coarsest order = 0
		}

		/*   the active V-cycle. */
		for (int outer = startOrder; outer < maxOrder; outer++)
		{
			order = outer;
			/* this interpolates boundary at start of loop, that does not seem logical.
			interpolate during upward part of the vCycle only.	*/
//			for (int iSolute : _soluteIndex)
//				_solute[iSolute].initLoop(order);

			// V-cycle loop.
			for (int v = 0; v < _vCycles; v++)
			{
				vc++;
				// Downward stroke of V.
				while ( order > 0 )
				{
					// Pre-smoothing.
					if( this.autoVcycleAdjust )
						relax( smoothingScalar * (order+1));
//						relax( Math.min( 5*(stage+1), nPreSteps) );
					else
						relax(nPreSteps);

					for (int iSolute : _soluteIndex)
						_solute[iSolute].downward1(order, outer);
					
					updateReacRateAndDiffRate(order-1);
					
					for (int iSolute : _soluteIndex)
						_solute[iSolute].downward2(order, outer);

					// Reduce grid value _g for good.
					order--;
				}
				
				// Bottom of V.
				if( this.autoVcycleAdjust )
					solveCoarsest( smoothingScalar );
				else
					solveCoarsest();
				
				// Upward stroke of V.
				while ( order < outer )
				{
					order++;
					for (int iSolute : _soluteIndex)
						_solute[iSolute].upward(order);

					for (int iSolute : _soluteIndex)
						_solute[iSolute].truncateConcToZero(order);

					// Post-smoothing.
					if( this.autoVcycleAdjust ) // smooth more if Vcycle becomes stagnant
						relax( smoothingScalar * (order+1) );
//						relax( Math.min( 5*(stage+1), nPostSteps) );
					else
						relax(nPostSteps);
				}

				/* Break the V-cycles if remaining error is dominated
				 * by local truncation error (see p. 884 of Numerical Recipes)
				 */
				breakVCycle = true;

				for (int iSolute : _soluteIndex)
					breakVCycle &= _solute[iSolute].breakVCycle(order);

				/* don't cycle initial coarse cycles */
				if (breakVCycle || outer < maxOrder-1)
				{
					breakVCycle = true;
					break;
				}

				/* if the solver struggles to reach stop conditions smooth more */
				smoothingScalar += 3;
			}
			if( ! breakVCycle && Log.shouldWrite( Log.Tier.CRITICAL ) )
				Log.out(Log.Tier.CRITICAL,
						"Warning: Multigrid VCycle stopped at maximum number of cycles.");
		}
		initialGuess = true;

		for (int iSolute : _soluteIndex)
		{
			for (int i = 0; i < maxOrder; i++)
			{
				if (!_solute[iSolute]._conc[i]._recordKeeper.isEmpty())
					for (RecordKeeper r : _solute[iSolute]._conc[i]._recordKeeper)
						r.flush();
			}
		}
		if( Log.shouldWrite( Log.Tier.EXPRESSIVE ) )
			Log.out(Log.Tier.EXPRESSIVE, "Vcycles: " + vc );

	}

	/**
	 * \brief Update concentration in the reactor.
	 */
	public void updateBulk()
	{
		/* Update reaction rates.
		 * This yields solute change rates in fg.L-1.hr-1
		 */
		//FIXME is this next line required?
//		updateReacRateAndDiffRate(maxOrder-1);

		/* Refresh the bulk concentration of the multigrids.
		*/
		for (int iSolute : _soluteIndex)
			_solute[iSolute].readBulk();
	}
	
	/**
	 * Solve the coarsest grid by relaxation Coarse grid is initialised to
	 * bulk concentration.
	 */
	public void solveCoarsest(int steps)
	{
		// NOTE disabled reset to bulk, previous solution should be better
		order = 0;
		// Reset coarsest grid to bulk concentration.
//		for (int iSolute : _soluteIndex)
//			_solute[iSolute].setWellmixed(order);

		// Relax NSOLVE times.
		relax(steps);
	}

	public void solveCoarsest() {
		solveCoarsest(nCoarseStep);
	}

	/**
	 * Apply nIter relaxations to the grid at the current resolution.
	 * Check here whether to continue with further post-steps?
	 * 
	 * @param nIter
	 */
	public void relax(int nIter)
	{
		for (int j = 0; j < nIter; j++)
		{
			updateReacRateAndDiffRate(order);
			for (int iSolute : _soluteIndex) {
				_solute[iSolute].relax(order);
			}
//
//			if (this._relaxationMap.values().contains(false))
//			{
//				updateReacRateAndDiffRate(order);
//				for (int iSolute : _soluteIndex)
//				{
//					if (!this._relaxationMap.get(_solute[iSolute].soluteName))
//					{
//						double[][][] difference = _solute[iSolute].relax(order);
//
//						double highestConc = Array.max(_solute[iSolute]._conc[order].grid);
//
//						if (Array.max(difference) < ((PDEWrapper)this._manager).absTol
//								|| Array.max(difference) < highestConc *
//								((PDEWrapper)this._manager).relTol)
//							this._relaxationMap.put(_solute[iSolute].soluteName, true);
//					}
//				}
//			}
//		 }
//		 for (String s : this._relaxationMap.keySet())
//		 {
//			 this._relaxationMap.put(s, false);
		 }
	}

	/**
	 * Call all the agents and read their uptake-rate for the current
	 * concentration.
	 * 
	 * @param resOrder
	 */
	public void updateReacRateAndDiffRate(int resOrder)
	{
		// Reset rates and derivative rates grids.
		for (int iSolute : _soluteIndex)
		{
			_solute[iSolute].resetReaction(resOrder);
			Array.restrictMinimum(_solute[iSolute]._conc[resOrder].grid, 0.0);
			allSolute[iSolute] = _solute[iSolute]._conc[resOrder];
			// TODO  environment reactions
			allReac[iSolute] = _solute[iSolute]._reac[resOrder];
			allDiffReac[iSolute] = _solute[iSolute]._diffReac[resOrder];
		}
		applyReaction(resOrder);
	}

	/**
	 * method original comming from reaction
	 * @param concGrid
	 * @param reacGrid
	 * @param diffReacGrid
	 * @param biomassGrid
	 */
	public void applyReaction(SolverGrid[] concGrid, SolverGrid[] reacGrid,
							  SolverGrid[] diffReacGrid, SolverGrid biomassGrid)
	{
		nSolute = concGrid.length;
		double[] s = Vector.zerosDbl(nSolute);
		int _nI, _nJ, _nK;
		_nI = biomassGrid.getGridSizeI();
		_nJ = biomassGrid.getGridSizeJ();
		_nK = biomassGrid.getGridSizeK();
		//globalReactionRate = 0;
		for (int i = 1; i<_nI+1; i++)
			for (int j = 1; j<_nJ+1; j++)
				for (int k = 1; k<_nK+1; k++)
				{
					// If there is no biomass, go to the next grid element
//					if  (biomassGrid.grid[i][j][k].equals(0.0) )
//						continue;
					// Read local solute concentration
					for (int iGrid : this._soluteIndex)
						s[iGrid] = concGrid[iGrid].grid[i][j][k];
					// First compute local uptake-rates in g.h-1
//					computeUptakeRate(s, biomassGrid.grid[i][j][k], 0.0);

					// Now add them on the received grids
					for (int iGrid : this._soluteIndex)
					{
//						reacGrid[iGrid].grid[i][j][k] += _uptakeRate[iGrid];
						// appears to always be 0.0 for typical monod or first order reactions
//						diffReacGrid[iGrid].grid[i][j][k] += _diffUptakeRate[iGrid];
						if (Double.isNaN(reacGrid[iGrid].grid[i][j][k]))
							Log.out("Warning: NaN generated in Reaction");
					}
				}
	}

	/**
	 * apply reactions to grid at specified resOrder
	 */
	public void applyReaction(int resorder)
	{
		double[] temp = new double[(allSolute[0]._is3D ? 3 : 2)];
		Vector.addEquals(temp, this._solute[0]._conc[resorder]._reso );
		((PDEWrapper) this._manager).applyReactions(this._solute, resorder, allReac, temp,
				Math.pow( this._solute[0]._conc[resorder]._reso, (allSolute[0]._is3D ? 3.0 : 2.0) ));

		/* synchronise cyclic reaction nodes */
		for( SolverGrid s : allReac )
		{
			s.syncBoundary(myDomain);
		}
	}

}
