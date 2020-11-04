package solver;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.DIFFUSIVITY;
import static grid.ArrayType.LOCALERROR;
import static grid.ArrayType.NONLINEARITY;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.RELATIVEERROR;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import grid.WellMixedConstants;
import linearAlgebra.Vector;
import shape.Shape;
import solver.multigrid.MultigridLayer;
import utility.ExtraMath;

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
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk) University of Nottingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
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
	/**
	 * number of V-cycles performed by multi-grid solver.
	 * (can try to increase when the solver appears to behave badly)
	 */
	private int _numVCycles = 1;
	/**
	 * maximum number of pre-steps
	 */
	private int _numPreSteps = 150;
	/**
	 * maximum number of coarse steps
	 */
	private int _numCoarseStep = 1500;
	/**
	 * maximum number of post steps
	 */
	private int _numPostSteps = 150; // 1 -> 1000, 0.5 -> 2500 seems to work
	
	/**
	 * Absolute threshold of the residual value at which relaxation is 
	 * interrupted.
	 * 
	 * NOTE: [Bas 2019] relative and absolute tolerance appear to just look at
	 * the multi-grid residual, I'm not sure whether that is completely correct.
	 * Looking at concentration changes between steps may be more appropriate.
	 */
	private double _absToleranceLevel;
	/**
	 * Relative threshold (relative to concentration) of the residual value at 
	 * which relaxation is interrupted. 
	 */
	private double _relToleranceLevel;
	/**
	 * Enable stopping relaxation when stop conditions are met
	 */
	private boolean _enableEarlyStop = false;
	/**
	 * Warn for large differences between Vcycle residuals (Debugging tool)
	 */
	private boolean _checkVCycleDiscrepancy = false;
	/**
	 * Difference between Vcycle residuals to warn at (Debugging tool)
	 */
	private double _discrepancyThreshold = 1.0;
	/**
	 * Stored old residual (Debugging tool internal use)
	 */
	private double tempRes[];
	/**
	 * Stored layer number (Debugging tool internal use)
	 */
	private int tempLay = 0;
	/**
	 * Stored Vcycle count (Debugging tool internal use)
	 */
	private int num = 0;
	/**
	 * Internal use, is set to true when stop condition is reached.
	 */
	private boolean _reachedStopCondition = false;

	/* ***********************************************************************
	 * Constructors
	 * **********************************************************************/
	/**
	 * Constructor for multigrid solver at default settings
	 */
	public PDEmultigrid()
	{

	}
	
	/**
	 * Constructor for multigrid solver at user suplied settings
	 * @param cycles
	 * @param pre
	 * @param coarse
	 * @param post
	 */
	public PDEmultigrid(int cycles, int pre, int coarse, int post)
	{
		if (cycles != 0)
			this._numVCycles = cycles;
		/**
		 * maximum number of pre-steps
		 */
		if (pre != 0)
			this._numPreSteps = pre;
		/**
		 * maximum number of coarse steps
		 */
		if (coarse != 0)
			this._numCoarseStep = coarse;
		/**
		 * maximum number of post steps
		 */
		if (post != 0)
			this._numPostSteps = post; // 1 -> 1000, 0.5 -> 2500 seems to work
	}
	
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
		for ( int outer = 1; outer < this._numLayers; outer++ )
		{
			this.setOrderOfAllMultigrids(variables, outer);
			
			/* TODO */
			MultigridLayer currentLayer;
			for ( SpatialGrid var : variables )
			{
				currentLayer = this.getMultigrid(var);
				currentLayer.fillArrayFromCoarser(CONCN, commonGrid);
				currentLayer.getGrid().reset(NONLINEARITY);
			}
			/* 
			 * Do V-cycles at this order until the limit is reached or the
			 * error is small enough.
			 */
			boolean continueVCycle = true;
			for ( int v = 0; v < this._numVCycles && continueVCycle; v++ )
				continueVCycle = this.doVCycle(variables, outer);
		}
	}

	private void refreshCommonGrid(SpatialGrid commonGrid)
	{
		/* Make the common multigrid if this is the first time. */
		if ( this._commonMultigrid == null )
		{
			this._commonMultigrid = 
					MultigridLayer.generateCompleteMultigrid(commonGrid);
			/* Make sure we have the finest grid to start with. */
			while ( this._commonMultigrid.hasFiner() )
				this._commonMultigrid = this._commonMultigrid.getFiner();
			/* Count the layers. */
			this._multigridShapes = new LinkedList<Shape>();
			MultigridLayer temp = this._commonMultigrid;
			this._multigridShapes.add(temp.getGrid().getShape());
			this._numLayers = 1;
			while ( temp.hasCoarser() )
			{
				temp = temp.getCoarser();
				this._multigridShapes.add(temp.getGrid().getShape());
				this._numLayers++;
			}
		}
		/* 
		 * Wipe all old values in the coarser layers, replacing them with the
		 * finest values.
		 */
		MultigridLayer.replaceAllLayersFromFinest(this._commonMultigrid);
		/*
		 * Tell all shapes to use red-black iteration.
		 */
		for ( Shape shape : this._multigridShapes )
			shape.setNewIterator(2);
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
	
	private void setMultigrid(MultigridLayer layer)
	{
		this._multigrids.put(layer.getGrid().getName(), layer);
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
				currentLayer.fillArrayFromFiner(type, 0.5, null);
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
			this.setMultigrid(currentLayer);
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
		while ( this._commonMultigrid.hasCoarser() )
			this._commonMultigrid = this._commonMultigrid.getCoarser();
		/* For each variable, find the coarsest layer and relax. */
		MultigridLayer currentLayer;
		for ( SpatialGrid var : variables )
		{
			currentLayer = this.getMultigrid(var);
			while ( currentLayer.hasCoarser() )
				currentLayer = currentLayer.getCoarser();
			this.setMultigrid(currentLayer);
		}
		
		this.relaxAll(this._numCoarseStep);
	}
	
	private boolean doVCycle(Collection<SpatialGrid> variables, int numLayers)
	{
		MultigridLayer variableMultigrid;
		SpatialGrid currentLayer, currentCommon;
		double truncationError, residual;
		int layerCounter = 0;
		/* Downward stroke of V. */
		while ( this._commonMultigrid.hasCoarser() && layerCounter < numLayers )
		{
			layerCounter++;
			/* 
			 * Smooth the current layer for a set number of iterations.
			 */
			/* Disabled Debug message 
			if ( Log.shouldWrite(Tier.DEBUG) )
			{
				Log.out(Tier.DEBUG, "Before pre-relaxing layer, concns in range:");
				double min, max;
				for ( SpatialGrid variable : variables )
				{
					currentLayer = this.getMultigrid(variable).getGrid();
					min = currentLayer.getMin(CONCN);
					max = currentLayer.getMax(CONCN);
					Log.out(Tier.DEBUG, "\t"+variable.getName()+": ["+min+", "+max+"]");
				}
			}
			*/
			
			this.relaxAll(this._numPreSteps);
			/* Disabled Debug message 
			if ( Log.shouldWrite(Tier.DEBUG) )
			{
				Log.out(Tier.DEBUG, "After pre-relaxing layer, concns in range:");
				double min, max;
				for ( SpatialGrid variable : variables )
				{
					currentLayer = this.getMultigrid(variable).getGrid();
					min = currentLayer.getMin(CONCN);
					max = currentLayer.getMax(CONCN);
					Log.out(Tier.DEBUG, "\t"+variable.getName()+": ["+min+", "+max+"]");
				}
			}
			*/
			/*
			 * Update the local truncation error using current CONCN values.
			 * In Numerical Recipes in C, this is is τ (tau) as defined in
			 * Equation (19.6.30).
			 */
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				currentLayer = this.getMultigrid(variable).getGrid();
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
				this.setMultigrid(variableMultigrid);
			}
			/*
			 * Restrict the concentration and local truncation errors from the
			 * finer layer to the coarser.
			 */
			Collection<SpatialGrid> currentGrids = new LinkedList<SpatialGrid>();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable);
				variableMultigrid.fillArrayFromFiner(
						CONCN, 0.5, currentCommon);
				variableMultigrid.fillArrayFromFiner(
						LOCALERROR, 0.5, currentCommon);
				variableMultigrid.fillArrayFromFiner(
						NONLINEARITY, 0.5, currentCommon);
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
		/* At the bottom of the V: solve the coarsest layer. */

		this.relaxAll(this._numCoarseStep);
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
		
		while ( this._commonMultigrid.hasFiner() && layerCounter > 0 )
		{
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable);
				currentLayer = variableMultigrid.getGrid();
				variableMultigrid.fillArrayFromFiner(
						LOCALERROR, CONCN, 0.0, currentCommon);
				currentLayer.subtractArrayFromArray(CONCN, LOCALERROR);
			}
			layerCounter--;
			this._commonMultigrid = this._commonMultigrid.getFiner();
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable).getFiner();
				this.setMultigrid(variableMultigrid);
				currentLayer = variableMultigrid.getGrid();
				variableMultigrid.fillArrayFromCoarser(
						RELATIVEERROR, CONCN, currentCommon);
				currentLayer.addArrayToArray(CONCN, RELATIVEERROR);
				if ( ! this._allowNegatives )
					currentLayer.makeNonnegative(CONCN);

// DEBUGGIMG
//				System.out.println(layerCounter + "\t" + currentLayer.getName() + " \t" + currentLayer.getMin(PRODUCTIONRATE) + " " );
//				System.out.println(layerCounter + "\t" + currentLayer.getName() + " \t" + currentLayer.getMin(CONCN) + " " );
			}
			/* Relaxation */
			this.relaxAll(this._numPostSteps);
		}
		/*
		 * Finally, we calculate the residual of the local truncation error and
		 * compare this with the residual of the relative truncation error
		 * calculated earlier. If the local truncation error of all variables
		 * dominates, then we can break the V-cycle.
		 * See p. 884 of Numerical Recipes in C for more details.
		 */
		boolean continueVCycle = true;
		currentCommon = this._commonMultigrid.getGrid();
		if( tempRes == null)
			tempRes = new double[variables.size()];
		int i = 0;

		for ( SpatialGrid variable : variables )
		{
			currentLayer = this.getMultigrid(variable).getGrid();
			this.calculateResidual(currentLayer, currentCommon, LOCALERROR);
			currentLayer.subtractArrayFromArray(LOCALERROR, NONLINEARITY);
			residual = currentLayer.getNorm(LOCALERROR);
			truncationError = this._truncationErrors.get(variable.getName());
			if ( this._checkVCycleDiscrepancy && 
					tempLay == numLayers && 
					tempRes[i] != 0.0 && 
					numLayers == this._numLayers-1 )
			{
				if( this.num > 1 )
				{
					double disc = Math.abs(this.tempRes[i]-residual) / Math.min(
							Math.abs(this.tempRes[i]), Math.abs(residual));
					if( disc > this._discrepancyThreshold && 
							Log.shouldWrite(Tier.CRITICAL))
						Log.out(Tier.CRITICAL, "Large V-Cycle discrepancy: " + 
								disc);
				}
				this.num++;
			}
			else
			{
				this.num = 0;
			}
			
			this.tempRes[i] = residual;
			i++;
			
			continueVCycle = (continueVCycle || residual > truncationError);
			if ( continueVCycle && Log.shouldWrite(Tier.DEBUG) )
				Log.out(Tier.DEBUG, "residual " + residual+ " > truncation"
						+ truncationError);
		}
		this.tempLay = numLayers;
		if ( continueVCycle && Log.shouldWrite(Tier.DEBUG))
			Log.out(Tier.DEBUG, "Continuing V-cycle");
		else if( Log.shouldWrite(Tier.DEBUG) )
			Log.out(Tier.DEBUG, "Breaking V-cycle");
		return continueVCycle;
	}
	
	private void relaxAll(int numRepetitions)
	{
		Collection<SpatialGrid> currentGrids = new LinkedList<SpatialGrid>();
		for ( MultigridLayer layer : this._multigrids.values() )
			currentGrids.add(layer.getGrid());
		SpatialGrid currentCommon = this._commonMultigrid.getGrid();
		
		int nGrid = currentGrids.size();
		double[][] validate = new double[nGrid][3];
		
		relaxLoops: for ( int i = 0; i < numRepetitions; i++ )
		{
			if(i >= numRepetitions-3)
			{
				int j = 0;
				for ( SpatialGrid grid : currentGrids ) 
				{
					validate[j++][i-(numRepetitions-3)] = grid.getAverage(CONCN);
				}
			}
//			System.out.println("r" + i);
			/* update reaction rate matrix at current concn */
			this._updater.prestep(currentGrids, 0.0);
			boolean stop = true;
			for ( SpatialGrid grid : currentGrids ) 
			{
				tempRes = new double[this._variableNames.length];
				this.relax(grid, currentCommon);
				if ( !this._reachedStopCondition )
					stop = false;
			}
			if ( stop ) {
//				if( Log.shouldWrite(Tier.DEBUG) )
					Log.out("Breaking early: "+ i +" of "
							+ numRepetitions );
				break relaxLoops;
			}
			if( i+1 >= numRepetitions && Log.shouldWrite(Tier.DEBUG) )
			{
				Log.out(Tier.DEBUG, i + " " + Vector.max(this.tempRes) + " > " +
						this._absToleranceLevel );
			}
		}
		/* update reaction rate matrix at current concn */
		this._updater.prestep(currentGrids, 0.0);
		
//		boolean periodic = false;
//		for ( int i = 0; i < validate.length; i++)
//			if ( ( validate[i][0] < validate[i][1] && 
//					validate[i][1] > validate[i][2]) ||
//					( validate[i][0] > validate[i][1] && 
//					validate[i][1] < validate[i][2]) )
//						periodic = true;
//		if ( periodic )
//			System.out.println(Matrix.toString(validate));
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
		int pos = 0;
		for (int i = 0; i < this._variableNames.length; i++)
		{
			if( variable.getName().equals(this._variableNames[i]))
				pos = i;
		}
		if ( ! this._allowNegatives )
			variable.makeNonnegative(CONCN);
		Shape shape = variable.getShape();
		/* Temporary storage. */
		double prod, concn, diffusivity, vol, rhs;
		double nhbDist, nhbSArea, nhbDiffusivity, nhbWeight, nhbConcn, bndryFlow;
		double lop, totalNhbWeight, residual;
		@SuppressWarnings("unused")
		int[] current, nhb;
		if ( this._enableEarlyStop  )
			this._reachedStopCondition = true;
		
		/* FIXME: inverting the order we iterate the grid changes the result
		 * I don't think the method should be sensitive to the direction of
		 * evaluation! Bas [09.12.2019]
		 */
		for ( current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{

			/* Skip this voxel if it is considered well-mixed. */
			if ( WellMixedConstants.isWellMixed(commonGrid, current))
				continue;
			concn = variable.getValueAtCurrent(CONCN);
			prod = variable.getValueAtCurrent(PRODUCTIONRATE);
			diffusivity = variable.getValueAtCurrent(DIFFUSIVITY);
			vol = shape.getCurrVoxelVolume();
			/* The right-hand side of Equation 19.6.23. */
			rhs = variable.getValueAtCurrent(NONLINEARITY);
			/* Reset both lop and dlop. */
			lop = 0.0;
			totalNhbWeight = 0.0;
			bndryFlow = 0.0;
			/* Sum up over all neighbours. */
			nhbLoop: for ( nhb = shape.resetNbhIterator();
					shape.isNbhIteratorValid(); nhb = shape.nbhIteratorNext() )
			{
				boolean isInside = shape.isNbhIteratorInside();
				/* First find the appropriate diffusivity. */
				if ( isInside )
				{
					/*
					 * If the neighbor voxel is inside the compartment, use the
					 * harmonic mean average diffusivity of the two voxels. 
					 */
					nhbDiffusivity = variable.getValueAtNhb(DIFFUSIVITY);
					nhbDiffusivity = 
							ExtraMath.harmonicMean(diffusivity, nhbDiffusivity);
				}
				else
				{
					/*
					 * If this is a boundary that does not contribute (e.g. a
					 * solid boundary) then do not include it in the weighting.
					 */
					bndryFlow = shape.nbhIteratorOutside()
							.getDiffusiveFlow(variable);
					if ( bndryFlow == 0.0 )
						continue nhbLoop;
					/*
					 * Otherwise, just use the current voxel's diffusivity.
					 */
					nhbDiffusivity = diffusivity;
				}
				nhbDist = shape.nhbCurrDistance();
				nhbSArea = shape.nhbCurrSharedArea();
				/*
				 * The weighting of each voxel is in terms of per time.
				 */
				nhbWeight = (nhbSArea * nhbDiffusivity) / (nhbDist * vol);
				totalNhbWeight += nhbWeight;
				/*
				 * The actual contribution of the neighbor voxel to the
				 * concentration of the current voxel depends on whether it is
				 * inside the compartment or on a boundary.
				 */
				if ( isInside )
				{
					nhbConcn = variable.getValueAtNhb(CONCN);
					lop += nhbWeight * (nhbConcn - concn);
				}
				else
				{
					/* Here we convert the flow (mass per time) into a change
					 * rate in concentration. */
					lop += bndryFlow / vol;
				}
			}
			/*
			 * For the FAS, the source term is implicit in the L-operator
			 * (see Equations 19.6.21-22).
			 * 
			 * NOTE: lop has units of concentration per time whereas prod would
			 * have units of mass per time
			 */
			lop += prod / vol;
			/* 
			 * TODO
			 */
			residual = (lop - rhs) / totalNhbWeight;
			this.tempRes[pos] = Math.max(this.tempRes[pos], Math.abs(residual));
			double relChange = residual / concn;
			/* Prepare to update the local concentration. */
			concn += residual;
			if (Math.abs(residual) > this._absToleranceLevel &&
					Math.abs(relChange) > this._relToleranceLevel) 
			{
				this._reachedStopCondition = false;
			} else {
				if( Log.shouldWrite(Tier.DEBUG) )
					Log.out(Tier.DEBUG, "residual = "+residual+" relChange = "+
							relChange );
			}
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
	/* Commented out is the older method of calculating the residual, verify
	   the new method and remove commented code when completely satisfied. */
//		Shape shape = variable.getShape();
//		double diffusiveFlow, rateFromReactions, residual;
//		
//		@SuppressWarnings("unused")
//		double prod, concn, diffusivity, vol, rhs;
//		double nhbDist, nhbSArea, nhbDiffusivity, nhbWeight, nhbConcn, bndryFlow;
//		double lop;
//		@SuppressWarnings("unused")
//		int[] current, nhb;
//		
//		for ( current = shape.resetIterator(); shape.isIteratorValid();
//				current = shape.iteratorNext() )
//		{
//			if ( WellMixedConstants.isWellMixed(commonGrid, current) )
//			{
//				/* Reset the value here in case it used to be inside the
//				 * boundary layer and move on to the next voxel. */
//				variable.setValueAt(destinationType, current, 0.0);
//				continue;
//			} 
//			
//			concn = variable.getValueAtCurrent(CONCN);
//			prod = variable.getValueAtCurrent(PRODUCTIONRATE);
//			diffusivity = variable.getValueAtCurrent(DIFFUSIVITY);
//			vol = shape.getCurrVoxelVolume();
//			/* The right-hand side of Equation 19.6.23. */
//			rhs = variable.getValueAtCurrent(NONLINEARITY);
//			/* Reset both lop and dlop. */
//			lop = 0.0;
//			bndryFlow = 0.0;
//			/* Sum up over all neighbours. */
//			nhbLoop: for ( nhb = shape.resetNbhIterator();
//					shape.isNbhIteratorValid(); nhb = shape.nbhIteratorNext() )
//			{
//				boolean isInside = shape.isNbhIteratorInside();
//				/* First find the appropriate diffusivity. */
//				if ( isInside )
//				{
//					/*
//					 * If the neighbor voxel is inside the compartment, use the
//					 * harmonic mean average diffusivity of the two voxels. 
//					 */
//					nhbDiffusivity = variable.getValueAtNhb(DIFFUSIVITY);
//					nhbDiffusivity = 
//							ExtraMath.harmonicMean(diffusivity, nhbDiffusivity);
//				}
//				else
//				{
//					/*
//					 * If this is a boundary that does not contribute (e.g. a
//					 * solid boundary) then do not include it in the weighting.
//					 */
//					bndryFlow = shape.nbhIteratorOutside()
//							.getDiffusiveFlow(variable);
//					if ( bndryFlow == 0.0 )
//						continue nhbLoop;
//					/*
//					 * Otherwise, just use the current voxel's diffusivity.
//					 */
//					nhbDiffusivity = diffusivity;
//				}
//				nhbDist = shape.nhbCurrDistance();
//				nhbSArea = shape.nhbCurrSharedArea();
//				/*
//				 * The weighting of each voxel is in terms of per time.
//				 */
//				nhbWeight = (nhbSArea * nhbDiffusivity) / (nhbDist * vol);
//				/*
//				 * The actual contribution of the neighbor voxel to the
//				 * concentration of the current voxel depends on whether it is
//				 * inside the compartment or on a boundary.
//				 */
//				if ( isInside )
//				{
//					nhbConcn = variable.getValueAtNhb(CONCN);
//					lop += nhbWeight * (nhbConcn - concn);
//				}
//				else
//				{
//					/* Here we convert the flow (mass per time) into a change
//					 * rate in concentration. */
//					lop += bndryFlow / vol;
//				}
//			}
//			diffusiveFlow = lop;
//			/*diffusiveFlow = 0.0;
//			for ( shape.resetNbhIterator(); shape.isNbhIteratorValid();
//					shape.nbhIteratorNext() )
//			{
//				diffusiveFlow += variable.getDiffusionFromNeighbor();
//			}*/
//			rateFromReactions = variable.getValueAt(PRODUCTIONRATE, current);
//			residual = (diffusiveFlow + rateFromReactions) /
//					shape.getCurrVoxelVolume();
//			variable.setValueAt(destinationType, current, residual);
//		}
		
		if ( ! this._allowNegatives )
			variable.makeNonnegative(CONCN);
		Shape shape = variable.getShape();
		/* Temporary storage. */
		double prod, concn, diffusivity, vol, rhs;
		double nhbDist, nhbSArea, nhbDiffusivity, nhbWeight, nhbConcn, bndryFlow;
		double lop, totalNhbWeight, residual;
		@SuppressWarnings("unused")
		int[] current, nhb;
		if ( this._enableEarlyStop  )
			this._reachedStopCondition = true;
		
		/* FIXME: inverting the order we iterate the grid changes the result
		 * I don't think the method should be sensitive to the direction of
		 * evaluation! Bas [09.12.2019]
		 */
		for ( current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{

			/* Skip this voxel if it is considered well-mixed. */
			if ( WellMixedConstants.isWellMixed(commonGrid, current))
				continue;
			concn = variable.getValueAtCurrent(CONCN);
			prod = variable.getValueAtCurrent(PRODUCTIONRATE);
			diffusivity = variable.getValueAtCurrent(DIFFUSIVITY);
			vol = shape.getCurrVoxelVolume();
			/* The right-hand side of Equation 19.6.23. */
			rhs = variable.getValueAtCurrent(NONLINEARITY);
			/* Reset both lop and dlop. */
			lop = 0.0;
			totalNhbWeight = 0.0;
			bndryFlow = 0.0;
			/* Sum up over all neighbours. */
			nhbLoop: for ( nhb = shape.resetNbhIterator();
					shape.isNbhIteratorValid(); nhb = shape.nbhIteratorNext() )
			{
				boolean isInside = shape.isNbhIteratorInside();
				/* First find the appropriate diffusivity. */
				if ( isInside )
				{
					/*
					 * If the neighbor voxel is inside the compartment, use the
					 * harmonic mean average diffusivity of the two voxels. 
					 */
					nhbDiffusivity = variable.getValueAtNhb(DIFFUSIVITY);
					nhbDiffusivity = 
							ExtraMath.harmonicMean(diffusivity, nhbDiffusivity);
				}
				else
				{
					/*
					 * If this is a boundary that does not contribute (e.g. a
					 * solid boundary) then do not include it in the weighting.
					 */
					bndryFlow = shape.nbhIteratorOutside()
							.getDiffusiveFlow(variable);
					if ( bndryFlow == 0.0 )
						continue nhbLoop;
					/*
					 * Otherwise, just use the current voxel's diffusivity.
					 */
					nhbDiffusivity = diffusivity;
				}
				nhbDist = shape.nhbCurrDistance();
				nhbSArea = shape.nhbCurrSharedArea();
				/*
				 * The weighting of each voxel is in terms of per time.
				 */
				nhbWeight = (nhbSArea * nhbDiffusivity) / (nhbDist * vol);
				totalNhbWeight += nhbWeight;
				/*
				 * The actual contribution of the neighbor voxel to the
				 * concentration of the current voxel depends on whether it is
				 * inside the compartment or on a boundary.
				 */
				if ( isInside )
				{
					nhbConcn = variable.getValueAtNhb(CONCN);
					lop += nhbWeight * (nhbConcn - concn);
				}
				else
				{
					/* Here we convert the flow (mass per time) into a change
					 * rate in concentration. */
					lop += bndryFlow / vol;
				}
			}
			/*
			 * For the FAS, the source term is implicit in the L-operator
			 * (see Equations 19.6.21-22).
			 * 
			 * NOTE: lop has units of concentration per time whereas prod would
			 * have units of mass per time
			 */
			lop += prod / vol;
			/* 
			 * TODO
			 */
			residual = (lop - rhs) / totalNhbWeight;
			double relChange = residual / concn;
			/* Prepare to update the local concentration. */
			concn += residual;
			if (Math.abs(residual) > this._absToleranceLevel &&
					Math.abs(relChange) > this._relToleranceLevel) 
			{
				this._reachedStopCondition = false;
			} else {
				if( Log.shouldWrite(Tier.DEBUG) )
					Log.out(Tier.DEBUG, "residual = "+residual+" relChange = "+
							relChange );
			}
			/* Check if we need to remain non-negative. */
			if ( (!this._allowNegatives) && (concn < 0.0) )
				concn = 0.0;
			/* Update the value and continue to the next voxel. */
			variable.setValueAt(destinationType, current, residual);
		}
	}
	
	@Override
	public void setAbsoluteTolerance(double tol) {
		this._absToleranceLevel = tol;
	}
	
	@Override
	public void setRelativeTolerance(double tol) {
		this._relToleranceLevel = tol;
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
