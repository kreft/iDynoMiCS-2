package solver;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.LOCALERROR;
import static grid.ArrayType.NONLINEARITY;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.RELATIVEERROR;
import static grid.ArrayType.WELLMIXED;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import grid.ArrayType;
import grid.SpatialGrid;
import shape.Shape;
import solver.multigrid.MultigridLayer;

/**
 * \brief Partial Differential Equation (PDE) solver that uses the Multi-Grid
 * layering approach to speed up a Gauss-Seidel iteration approach. This PDE
 * solver can only solve to steady-state, and should not be used where a
 * time-dependent solution is appropriate.
 * 
 * <p>For reference, see <i>Numerical Recipes in C</i> (Press, Teukolsky,
 * Vetterling & Flannery, 1997), Chapter 19.6: Multigrid Methods for Boundary
 * Value Problems. Equation numbers used in this chapter will be referenced
 * throughout the class source code. Due to the non-linear nature of many
 * reaction kinetics, this solver implements the Full Approximation Storage
 * (FAS) Algorithm discussed towards the end of the chapter.</p>
 * 
 * <p>Here are the meanings of the various symbols used in that chapter,
 * within the context of iDynoMiCS 2:<ul>
 * <li><i>u</i>, the variable, is concentration</li>
 * <li><i>L</i>, the linear elliptic operator, is diffusion</li>
 * <li><i>f</i>, the source term, is production/consumption from reactions</li>
 * </ul></p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEmultigrid extends PDEsolver
{
	
	private Map<String, MultigridLayer> _multigrids = 
			new HashMap<String, MultigridLayer>();
	
	private MultigridLayer _commonMultigrid;
	
	private List<Shape> _multigridShapes;
	
	private Map<String, Double> _truncationErrors =
			new HashMap<String, Double>();
	/**
	 * Number of layers in the multigrid being used. Calculated when
	 * {@link #_commonMultigrid} is constructed and must never be changed
	 * afterwards.
	 */
	private int _numLayers;
	
	private int _numVCycles = 100;
	
	private int _numPreSteps = 100;
	
	private int _numCoarseStep = 100;
	
	private int _numPostSteps = 100;
	/**
	 * TODO this should be settable by the user.
	 */
	private double _boundaryThreshold = 1.0;
	
	/* ***********************************************************************
	 * SOLVER METHODS
	 * **********************************************************************/
	
	@Override
	public Collection<Shape> getShapesForAgentMassDistributionMaps(
			SpatialGrid commonGrid)
	{
		this.refreshCommonGrid(commonGrid);
		return this._multigridShapes;
	}
	
	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		this.refreshCommonGrid(commonGrid);
		for ( SpatialGrid var : variables )
			this.refreshVariable(var);
		
		this.solveCoarsest(variables);
		
		/* See Figure 19.6.2 */
		for ( int outer = 1; outer <= this._numLayers; outer++ )
		{
			this.setOrderOfAllMultigrids(variables, outer);
			
			/* TODO */
			MultigridLayer currentLayer;
			for ( SpatialGrid var : variables )
			{
				currentLayer = this.getMultigrid(var);
				currentLayer.fillArrayFromCoarser(CONCN);
				currentLayer.getGrid().reset(NONLINEARITY);
			}
			/* 
			 * Do V-cycles at this order until the limit is reached or the
			 * error is small enough.
			 */
			boolean continueVCycle = true;
			for ( int v = 0; v < this._numVCycles && continueVCycle; v++ )
				continueVCycle = this.DoVCycle(variables, outer);
		}
		
		/* Apply all computations. */
		// TODO Rob [15Jan2017]: Not sure this is necessary, try removing once
		// everything is working.
		MultigridLayer currentLayer;
		for ( SpatialGrid var : variables )
		{
			currentLayer = this.getMultigrid(var);
			while ( currentLayer.hasFiner() )
				currentLayer = currentLayer.getFiner();
			var.setTo(CONCN, currentLayer.getGrid().getArray(CONCN));
		}
	}

	private void refreshCommonGrid(SpatialGrid commonGrid)
	{
		/* Make the common multigrid if this is the first time. */
		if ( this._commonMultigrid == null )
		{
			this._commonMultigrid = 
					MultigridLayer.generateCompleteMultigrid(commonGrid);
			/* Count the layers. */
			this._numLayers = 0;
			this._multigridShapes = new LinkedList<Shape>();
			for ( MultigridLayer temp = this._commonMultigrid;
					temp.hasCoarser(); temp = temp.getCoarser() )
			{
				this._multigridShapes.add(temp.getGrid().getShape());
				this._numLayers++;
			}
		}
		/* 
		 * Wipe all old values in the coarser layers, replacing them with the
		 * finest values.
		 */
		MultigridLayer.replaceAllLayersFromFinest(this._commonMultigrid);
	}
	
	public MultigridLayer getMultigrid(SpatialGrid variable)
	{
		String name = variable.getName();
		if ( this._multigrids.containsKey(name) )
			return this._multigrids.get(name);
		/* New variable, so we need to make the MultigridLayer. */
		MultigridLayer newMultigrid = MultigridLayer.generateCompleteMultigrid(
				variable, this._multigridShapes);
		this._multigrids.put(name, newMultigrid);
		return newMultigrid;
	}
	
	private void refreshVariable(SpatialGrid variable)
	{
		MultigridLayer currentLayer = this.getMultigrid(variable);
		// TODO Rob [15Jan2017]: Not sure this is necessary, try removing once
		// everything is working.
		for ( ArrayType type : variable.getAllArrayTypes() )
			currentLayer.getGrid().setTo(type, variable.getArray(type));
		currentLayer.getGrid().newArray(NONLINEARITY);
		currentLayer.getGrid().newArray(LOCALERROR);
		currentLayer.getGrid().newArray(RELATIVEERROR);
		currentLayer.getGrid().newArray(PRODUCTIONRATE);
		while ( currentLayer.hasCoarser() )
		{
			currentLayer = currentLayer.getCoarser();
			// NOTE iDynoMiCS 1 uses fracOfOldValueKept of 0.5
			for ( ArrayType type : variable.getAllArrayTypes() )
				currentLayer.fillArrayFromFiner(type, 0.0);
		}
	}
	
	/**
	 * \brief Set the current multigrid layer for all variables to the order
	 * given.
	 * 
	 * <p>An order of zero is the coarsest (i.e. fewest grid voxels), so
	 * calling this method with an order ≤ 0 will set all multigrids to the
	 * coarsest layer.</p>
	 * 
	 * @param variables Collection of variables to set for.
	 * @param order The greater this number, the finer the grid layers.
	 */
	private void setOrderOfAllMultigrids(
			Collection<SpatialGrid> variables, int order)
	{
		MultigridLayer currentLayer;
		for ( SpatialGrid var : variables )
		{
			currentLayer = this.getMultigrid(var);
			/* Find the coarsest layer. */
			while ( currentLayer.hasCoarser() )
				currentLayer = currentLayer.getCoarser();
			/* Go finer for "order" number of layers. */
			for ( int i = 0; i < order; i++ )
				currentLayer = currentLayer.getFiner();
			this._multigrids.put(var.getName(), currentLayer);
		}
		
		while ( this._commonMultigrid.hasCoarser() )
			this._commonMultigrid = this._commonMultigrid.getCoarser();
		/* Go finer for "order" number of layers. */
		for ( int i = 0; i < order; i++ )
			this._commonMultigrid = this._commonMultigrid.getFiner();
	}
	
	private void solveCoarsest(Collection<SpatialGrid> variables)
	{
		/* Find the coarsest layer of the common grid. */
		MultigridLayer common = this._commonMultigrid;
		while ( common.hasCoarser() )
			common = common.getCoarser();
		/* For each variable, find the coarsest layer and relax. */
		MultigridLayer currentLayer;
		Collection<SpatialGrid> currentGrids = new LinkedList<SpatialGrid>();
		for ( SpatialGrid var : variables )
		{
			currentLayer = this.getMultigrid(var);
			while ( currentLayer.hasCoarser() )
				currentLayer = currentLayer.getCoarser();
			currentGrids.add(currentLayer.getGrid());
		}
		
		this._updater.prestep(currentGrids, 0.0);
		
		for ( SpatialGrid currentGrid : currentGrids )
		{
			for ( int i = 0; i < this._numCoarseStep; i++ )
				this.relax(currentGrid, common.getGrid());
		}
	}
	
	private boolean DoVCycle(Collection<SpatialGrid> variables, int numLayers)
	{
		MultigridLayer variableMultigrid;
		SpatialGrid currentLayer, currentCommon;
		double truncationError;
		int layerCounter = 0;
		/* Downward stroke of V. */
		while ( this._commonMultigrid.hasCoarser() && layerCounter < numLayers )
		{
			layerCounter++;
			/* 
			 * Smooth the current layer for a set number of iterations and then
			 * update the local truncation error using current CONCN values.
			 * In Numerical Recipes in C, this is is τ (tau) as defined in
			 * Equation (19.6.30).
			 */
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				currentLayer = this.getMultigrid(variable).getGrid();
				/*  */
				for ( int i = 0; i < this._numPreSteps; i++ )
					this.relax(currentLayer, currentCommon);
				this.calculateResidual(currentLayer, currentCommon, LOCALERROR);
			}
			/*
			 * Find the coarser layer for the common grid and all variables.
			 */
			this._commonMultigrid = this._commonMultigrid.getCoarser();
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable).getCoarser();
				this._multigrids.put(variable.getName(), variableMultigrid);
			}
			/*
			 * Restrict the concentration and local truncation errors from the
			 * finer layer to the coarser.
			 */
			Collection<SpatialGrid> currentGrids = new LinkedList<SpatialGrid>();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable);
				variableMultigrid.fillArrayFromFiner(CONCN, 0.5);
				variableMultigrid.fillArrayFromFiner(LOCALERROR, 0.5);
				variableMultigrid.fillArrayFromFiner(NONLINEARITY, 0.5);
				currentGrids.add(variableMultigrid.getGrid());
			}
			/* Update the PRODUCTIONRATE arrays using updated CONCN values. */
			this._updater.prestep(currentGrids, 0.0);
			
			/*
			 * TODO
			 * The relative truncation error is the difference between the
			 * restricted local truncation error and
			 * Equation 19.6.32/34/35???
			 */
			for ( SpatialGrid variable : variables )
			{
				currentLayer = this.getMultigrid(variable).getGrid();
				this.calculateResidual(currentLayer, currentCommon, RELATIVEERROR);
				currentLayer.subtractArrayFromArray(RELATIVEERROR, LOCALERROR);
				// TODO work out what this is for!!!
				currentLayer.addArrayToArray(NONLINEARITY, RELATIVEERROR);
				// TODO only do this if "order+1 == outer"
				truncationError = currentLayer.getNorm(RELATIVEERROR);
				this._truncationErrors.put(variable.getName(), truncationError);
			}
		}
		/* 
		 * At the bottom of the V: solve the coarsest layer.
		 */
		currentCommon = this._commonMultigrid.getGrid();
		for ( SpatialGrid variable : variables )
		{
			currentLayer = this.getMultigrid(variable).getGrid();
			for ( int i = 0; i < this._numCoarseStep; i++ )
				this.relax(currentLayer, currentCommon);
		}
		/* 
		 * Upward stroke of V. The overall effect of this is:
		 * 
		 * Coarser.LocalError = Restricted(Finer.Concn)
		 * 
		 * Coarser.Concn -= Restricted(Finer.Concn)
		 * 
		 * [Eq (19.6.28)]
		 * Finer.RelativeError = Interpolated(Coarser.Concn - Restricted(Finer.Concn))
		 * 
		 * [Eq (19.6.29)]
		 * Finer.Concn += Interpolated(Coarser.Concn - Restricted(Finer.Concn))
		 * 
		 * Followed by making values non-negative (if required) and
		 * post-relaxation.
		 */
		// TODO find corresponding parts in Numerical Recipes
		while ( this._commonMultigrid.hasFiner() )
		{
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable);
				currentLayer = variableMultigrid.getGrid();
				variableMultigrid.fillArrayFromFiner(CONCN, LOCALERROR, 0.5);
				currentLayer.subtractArrayFromArray(CONCN, LOCALERROR);
			}
			this._commonMultigrid = this._commonMultigrid.getFiner();
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable).getFiner();
				this._multigrids.put(variable.getName(), variableMultigrid);
				currentLayer = variableMultigrid.getGrid();
				variableMultigrid.fillArrayFromCoarser(RELATIVEERROR, CONCN);
				currentLayer.addArrayToArray(CONCN, RELATIVEERROR);
				if ( ! this._allowNegatives )
					currentLayer.makeNonnegative(CONCN);
				for ( int i = 0; i < this._numPostSteps; i++ )
					this.relax(currentLayer, currentCommon);
				
				
			}
		}
		/*
		 * Finally, we calculate the residual of the local truncation error and
		 * compare this with the residual of the relative truncation error
		 * calculated earlier. If the local truncation error of all variables
		 * dominates, then we can break the V-cycle.
		 * See p. 884 of Numerical Recipes in C for more details.
		 */
		boolean continueVCycle = false;
		for ( SpatialGrid variable : variables )
		{
			currentLayer = this.getMultigrid(variable).getGrid();
			this.calculateResidual(currentLayer, currentCommon, LOCALERROR);
			// TODO LOCALERROR -= RHS ???
			truncationError = currentLayer.getNorm(LOCALERROR);
			continueVCycle = truncationError > this._truncationErrors.get(variable.getName());
			if ( continueVCycle )
				break;
		}
		return continueVCycle;
	}
	
	/**
	 * \brief The method used for smoothing a grid. Here we use a Gauss-Seidel
	 * iteration scheme with Red-Black iteration.
	 * 
	 * <p>This method corresponds to Equation (19.6.12) in <i>Numerical Recipes
	 * in C</i>:<br><i>u<sub>i</sub> = - L<sub>ii</sub><sup>-1</sup> 
	 * ( sum<sub>j≠i</sub> [ L<sub>ij</sub> u<sub>j</sub> ] - f<sub>i</sub> )
	 * </i><br>where we interpret<ul>
	 * <li><i>L<sub>ij</sub></i> as the rate of diffusion into voxel <i>i</i></li>
	 * <li><i>f</i> as the rate of production due to reactions</li>
	 * </ul></p>
	 * 
	 * @param variable Spatial grid representation of a solute field.
	 * @param commonGrid Common store of the well-mixed array for all variables.
	 */
	private void relax(SpatialGrid variable, SpatialGrid commonGrid)
	{
		Shape shape = variable.getShape();
		/* Temporary storage. */
		double concn, invVol, lop, dlop, rhs, res;
		for ( int[] current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			if ( commonGrid.getValueAt(WELLMIXED, current) >= 
					this._boundaryThreshold )
			{
				continue;
			}
			concn = variable.getValueAtCurrent(CONCN);
			invVol = 1.0 / shape.getCurrVoxelVolume();
			/* Reset both lop and dlop. */
			lop = 0.0;
			dlop = 0.0;
			/* Sum up over all neighbours. */
			for ( shape.resetNbhIterator(); shape.isNbhIteratorValid();
					shape.nbhIteratorNext() )
			{
				dlop += variable.getDiffusiveTimeScaleWithNeighbor();
				lop += variable.getDiffusionFromNeighbor();
			}
			/* Convert lop and dlop from mass/time to concentration/time. */
			lop *= invVol;
			dlop *= invVol;
			/*
			 * For the FAS, the source term is implicit in the L-operator
			 * (see Equations 19.6.21-22).
			 */
			lop += variable.getValueAtCurrent(PRODUCTIONRATE);
			/* The right-hand side of Equation 19.6.23. */
			rhs = variable.getValueAtCurrent(NONLINEARITY);
			/* TODO */
			res = (lop - rhs)/dlop;
			/* TODO */
			concn += res;
			/* Check if we need to remain non-negative. */
			if ( (!this._allowNegatives) && (concn < 0.0) )
				concn = 0.0;
			/* Update the value and continue to the next voxel. */
			variable.setValueAtCurrent(CONCN, concn);
		}
	}
	
	/**
	 * \brief Estimate the remaining work that needs to be done on the grid
	 * given.
	 * 
	 * <p>This method corresponds to Equation (19.6.4) in <i>Numerical Recipes
	 * in C</i>.</p>
	 * 
	 * @param variable Spatial grid representation of a solute field.
	 * @param commonGrid Common store of the well-mixed array for all variables.
	 * @param destinationType Type of array to overwrite with the new values.
	 */
	private void calculateResidual(SpatialGrid variable,
			SpatialGrid commonGrid, ArrayType destinationType)
	{
		Shape shape = variable.getShape();
		double diffusiveFlow, rateFromReactions, residual;
		for ( int[] current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			if ( commonGrid.getValueAt(WELLMIXED, current) >= 
					this._boundaryThreshold )
			{
				/* Reset the value here in case it used to be inside the
				 * boundary layer and move on to the next voxel. */
				variable.setValueAt(destinationType, current, 0.0);
				continue;
			} 
			diffusiveFlow = 0.0;
			for ( shape.resetNbhIterator(); shape.isNbhIteratorValid();
					shape.nbhIteratorNext() )
			{
				diffusiveFlow += variable.getDiffusionFromNeighbor();
			}
			rateFromReactions = variable.getValueAt(PRODUCTIONRATE, current);
			residual = (diffusiveFlow + rateFromReactions) /
					shape.getCurrVoxelVolume();
			variable.setValueAt(destinationType, current, residual);
		}
	}

	/* ***********************************************************************
	 * WELL-MIXED CHANGES
	 * **********************************************************************/
	
	@Override
	protected double getWellMixedFlow(String name)
	{
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	protected void increaseWellMixedFlow(String name, double flow)
	{
		// TODO Auto-generated method stub
	}
}
