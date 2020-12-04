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
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.Idynomics;
import processManager.ProcessDiffusion;
import processManager.ProcessManager;
import settable.Settable;

import java.util.ArrayList;
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
	
//	protected SoluteGrid[]		allReac;
	
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
	/**
	 * 
	 */
	public void init(Domain domain, EnvironmentContainer environment,
					 AgentContainer agents, ProcessDiffusion manager,
					 int vCycles, int preSteps, int coarseSteps, int postSteps)
	{
		/* Get the computational domain that this solver is associated with. */
		myDomain = domain;

		this._manager = manager;

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

		// TODO handle idyno 2 reactions

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
//		allReac = new SoluteGrid[nSolute];
		allDiffReac = new SoluteGrid[nSolute];

		/* TODO both soluteList entry 0, eh? */
		_bLayer = new MultigridSolute(_soluteList.get(0), "boundary layer");
		_diffusivity = 
				new MultigridSolute(_soluteList.get(0), "relative diffusivity");
		
		Double sBulk;
		for (int i = 0; i < nSolute; i++)
		{
//			if (_soluteIndex.contains(i))
//			{
				sBulk = 0.0; // initial value
				_solute[i] = new MultigridSolute(_soluteList.get(i),
												_diffusivity, _bLayer, sBulk);
//			}
//			else
//				_solute[i] = null;
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
		
		/* TODO idyno 2 reaction handling
		 * TODO we do want a biomass grid to determine diffusivity regimes but
		 *  we don't want them for their reactions as this will be handled the
		 * 	new way.
		 *
		 * Initialize array of reactive biomasses.
		 */
//		_biomass = new MultigridSolute[nReaction];
//		for (int i = 0; i < nReaction; i++)
//		{
//			_biomass[i] = new MultigridSolute(_soluteList[0],
//											_reactions.get(i).reactionName);
//			_biomass[i].resetMultigridCopies(0.0);
//		}
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
//		myDomain.refreshBioFilmGrids();
		
		// TODO this is the region in which diffusion is solved?
		_bLayer.setFinest( myDomain.getBoundaryLayer() );
		_bLayer.restrictToCoarsest();
		
		// TODO this should be per solute no?
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
		 */
		while ( timeToSolve > 0 )
		{
			// Compute new equilibrium concentrations.
			stepSolveDiffusionReaction();
			
			// Update bulk concentration.
			updateBulk();
			
			// Manage iterations.
			internalIteration += 1;
			timeToSolve -= internTimeStep;
		}

		// Apply results on solute grids
		for (int iSolute : _soluteIndex)
			_solute[iSolute].applyComputation();

	}

	/**
	 * One step of the solver
	 */
	public void stepSolveDiffusionReaction()
	{
		for (int iSolute : _soluteIndex)
			_solute[iSolute].resetMultigridCopies();

		// Solve chemical concentrations on coarsest grid.
		solveCoarsest();

		/* TODO order / outer representing finer and coarse grid for
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

				updateReacRateAndDiffRate(order);
				for (int iSolute : _soluteIndex)
					breakVCycle &= _solute[iSolute].breakVCycle(order, v);

				if (breakVCycle)
					break;
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
		
		// TODO update boundaries?
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
		order = 0;
		// Reset coarsest grid to bulk concentration.
		for (int iSolute : _soluteIndex)
			_solute[iSolute].setSoluteGridToBulk(order);

		// Relax NSOLVE times.
		relax(nCoarseStep);
	}

	/**
	 * Apply nIter relaxations to the grid at the current resolution.
	 * 
	 * @param nIter
	 */
	public void relax(int nIter)
	{
		for (int j = 0; j < nIter; j++)
		{
			updateReacRateAndDiffRate(order);
			for (int iSolute : _soluteIndex)
				_solute[iSolute].relax(order);
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
			allSolute[iSolute] = _solute[iSolute]._conc[resOrder];
			
			// TODO reactions
//			allReac[iSolute] = _solute[iSolute]._reac[resOrder];
			allDiffReac[iSolute] = _solute[iSolute]._diffReac[resOrder];
		}

		// TODO agent and environment reaction rate
//		// Calls the agents of the guild and sums their uptake-rate
//		for (int iReac = 0; iReac<_reactions.size(); iReac++)
//			_reactions.get(iReac).applyReaction(allSolute, allReac,
//								allDiffReac, _biomass[iReac]._conc[resOrder]);
		/*
		 *  computes uptake rate per solute ( mass*_specRate*this._soluteYield[iSolute]; )
		 *  reactionGrid += uptakeRateGrid
		 *   diffReactionGrid += diffUptakeRate ( no diffusion mediated by agents)
		 */
		this._manager.prestep( this._environment.getSolutes(), 0.0 );

		/* TODO flash current concentration to iDyno 2 concentration grids */

		for( MultigridSolute s : _solute )
		{
			/* set the net production to the finest grid */
			s._reac[ 0 ] = new SoluteGrid(this.myDomain, s.soluteName,
					ArrayType.PRODUCTIONRATE, this._environment.getSoluteGrid( s.soluteName ) );
		}
	}

}
