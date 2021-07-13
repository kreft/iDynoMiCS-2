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

import agent.Agent;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.Log;
import debugTools.QuickCSV;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Idynomics;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import processManager.ProcessDiffusion;
import processManager.ProcessManager;
import processManager.library.PDEWrapper;
import settable.Settable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

	protected ProcessDiffusion _manager;

	protected EnvironmentContainer _environment;
	
	protected LinkedList<RecordKeeper> _recordKeepers;
	/**
	 * 
	 */
	public void init(Domain domain, EnvironmentContainer environment,
					 AgentContainer agents, PDEWrapper manager,
					 int vCycles, int preSteps, int coarseSteps, int postSteps)
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

		/* reaction handling done with iDyno 2 classes */
//		for (String aReacName : xmlRoot.getChildrenNames("reaction"))
//			addReactionWithSolutes(aSim.getReaction(aReacName));
		
		nCoarseStep = coarseSteps;
		_vCycles = vCycles;
		nPreSteps = preSteps;
		nPostSteps = postSteps;

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
		/*
		LogFile.writeLogAlways("Solver_multigrid.initializeConcentrationfields()");
		LogFile.writeLogAlways("Padded range is "+_solute[0].getGrid().getMin()+
								" to "+_solute[0].getGrid().getMax());
		LogFile.writeLogAlways("Unpadded range is "+_solute[0].getGrid().getMinUnpadded()+
										" to "+_solute[0].getGrid().getMaxUnpadded());
		*/
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
			// Compute new equilibrium concentrations.
			stepSolveDiffusionReaction();
			
			// Update bulk concentration.
			updateBulk();
			
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
			double[][][] out = MultigridUtils.translateOut(
					_solute[iSolute].realGrid.grid );
			this._environment.getSoluteGrid( this._solute[iSolute].soluteName ).
					setTo(ArrayType.CONCN, out );
		}

//		((PDEWrapper) this._manager).flashConcentrations(allSolute);
//
//		for (int iSolute : _soluteIndex)
//			Log.out(Array.toString(_solute[iSolute].realGrid.getGrid()));

	}

	/**
	 * One step of the solver
	 */
	public void stepSolveDiffusionReaction()
	{
		for (int iSolute : _soluteIndex)
			_solute[iSolute].resetMultigridCopies();

		// Solve chemical concentrations on coarsest grid.
		solveCoarsest(); // coarsest order = 0

		/*

		The outer loop is getting finer (starting from coarsest + 1
		The inner loop is getting coarser and then finer

		        /\    /
		   /\  /  \  /
		/\/  \/    \/

		 */

		/* order / outer representing finer and coarse grid for
		*   the active V-cycle. */
		// Nested iteration loop.
		for (int outer = 1; outer < maxOrder; outer++)
		{
			order = outer;
			for (int iSolute : _soluteIndex)
				_solute[iSolute].initLoop(order);

			// V-cycle loop.
			for (int v = 0; v < _vCycles; v++)
			{
				// Downward stroke of V.
				while ( order > 0 )
				{
					// Pre-smoothing.
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
					relax(nPostSteps);
				}

				/* Break the V-cycles if remaining error is dominated
				 * by local truncation error (see p. 884 of Numerical Recipes)
				 */
				boolean breakVCycle = true;

				/* TODO validate (this one seems to be unnecessary) */
//				updateReacRateAndDiffRate(order);
				for (int iSolute : _soluteIndex)
					breakVCycle &= _solute[iSolute].breakVCycle(order, v);

				if (breakVCycle)
					break;
			}
		}
		for (int iSolute : _soluteIndex)
		{
			for (int i = 0; i < maxOrder; i++)
			{
				if (!_solute[iSolute]._conc[i]._recordKeeper.isEmpty())
					for (RecordKeeper r : _solute[iSolute]._conc[i]._recordKeeper)
						r.flush();
			}
		}
	}

	/**
	 * \brief Update concentration in the reactor.
	 *
	 * TODO boundaries update
	 */
	public void updateBulk()
	{
		/* Update reaction rates.
		 * This yields solute change rates in fg.L-1.hr-1
		 */
		updateReacRateAndDiffRate(maxOrder-1);

		// TODO update bulk boundary, we currently work with fixed concentration only
		// Find the connected bulks and agars and update their concentration.
//		for (AllBC aBC : myDomain.getAllBoundaries())
//		{
//			if ( aBC instanceof ConnectedBoundary )
//			{
//				((ConnectedBoundary) aBC).
//							updateBulk(allSolute, allReac, internTimeStep);
//			}
//			if ( aBC instanceof BoundaryAgar )
//			{
//				((BoundaryAgar) aBC).
//							updateAgar(allSolute, allReac, internTimeStep);
//			}
//		}
		
		// Refresh the bulk concentration of the multigrids.
		for (int iSolute : _soluteIndex)
			_solute[iSolute].readBulk();
	}
	
	/**
	 * Solve the coarsest grid by relaxation Coarse grid is initialised to
	 * bulk concentration.
	 */
	public void solveCoarsest()
	{
		// NOTE disabled reset to bulk, previous solution should be better
		order = 0;
		// Reset coarsest grid to bulk concentration.
//		for (int iSolute : _soluteIndex)
//			_solute[iSolute].setSoluteGridToBulk(order);

		// Relax NSOLVE times.
		relax(nCoarseStep);
	}

	/**
	 * Apply nIter relaxations to the grid at the current resolution.
	 * Check here whether to continue with further post-steps?
	 * 
	 * @param nIter
	 */
	public void relax(int nIter)
	{
		HashMap<String, Boolean> relaxationMap = new HashMap<String, Boolean>();
		
		for (int iSolute : _soluteIndex)
		{
			relaxationMap.put(_solute[iSolute].soluteName, false);
		}
		
		//while (relaxationMap.entrySet().contains(false))
		//{
		
			for (int j = 0; j < nIter; j++)
			{
				updateReacRateAndDiffRate(order);
				
				for (int iSolute : _soluteIndex)
				{
					if (!relaxationMap.get(_solute[iSolute].soluteName))
					{
						double[][][] difference = _solute[iSolute].relax(order);
						
						if (Array.max(difference) < ((PDEWrapper)this._manager).absTol)
							relaxationMap.put(_solute[iSolute].soluteName, true);
					}
				}
			}
		//}
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
			allSolute[iSolute] = _solute[iSolute]._conc[resOrder];
			// TODO  environment reactions
			allReac[iSolute] = _solute[iSolute]._reac[resOrder];
			allDiffReac[iSolute] = _solute[iSolute]._diffReac[resOrder];
		}

		// TODO agent and environment reaction rate
		// Calls the agents of the guild and sums their uptake-rate
//		for (int iReac = 0; iReac<_reactions.size(); iReac++)
//			_reactions.get(iReac).applyReaction(allSolute, allReac,
//								allDiffReac, _biomass[iReac]._conc[resOrder]);

		// TODO is this method assuming finest grid or correctly using resOrder?
		applyReaction(resOrder);
		/*
		 *  computes uptake rate per solute ( mass*_specRate*this._soluteYield[iSolute]; )
		 *  reactionGrid += uptakeRateGrid
		 *   diffReactionGrid += diffUptakeRate ( no diffusion mediated by agents)
		 */
//		this._manager.prestep( this._environment.getSolutes(), 0.0 );

		/* flash current concentration to iDyno 2 concentration grids
		Note This hapens after the solver has finished, during solver
		we use temporary grids with applyReaction()
		((PDEWrapper) this._manager).flashConcentrations(allSolute); */
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
	 * TODO done at different depths?
	 */
	public void applyReaction(int resorder)
	{
		double[] temp = new double[(allSolute[0]._is3D ? 3 : 2)];
		Vector.addEquals(temp, this._solute[0]._conc[resorder]._reso );
		((PDEWrapper) this._manager).applyReactions(this._solute, resorder, allReac,	temp,
				Math.pow( this._solute[0]._conc[resorder]._reso, 3.0 ));
	}

}
