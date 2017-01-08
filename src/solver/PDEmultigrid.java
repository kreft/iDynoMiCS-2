package solver;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.CUMULATIVEERROR;
import static grid.ArrayType.LOCALERROR;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.RELATIVEERROR;
import static grid.ArrayType.WELLMIXED;

import java.util.Collection;
import java.util.HashMap;
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
 * throughout the class source code.</p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEmultigrid extends PDEsolver
{
	
	private Map<String, MultigridLayer> _multigrids = 
			new HashMap<String, MultigridLayer>();
	
	private MultigridLayer _commonMultigrid;
	
	private Map<String, Double> _truncationErrors =
			new HashMap<String, Double>();
	
	private int _numVCycles;
	
	private int _numPreSteps;
	
	private int _numPostSteps;
	/**
	 * TODO this should be settable by the user.
	 */
	private double _boundaryThreshold = 1.0;
	
	/* ***********************************************************************
	 * SOLVER METHODS
	 * **********************************************************************/
	
	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		this.refreshCommonGrid(commonGrid);
		
		MultigridLayer variableMultigrid;
		SpatialGrid currentLayer, currentCommon;
		double truncationError;
		
		
		
		/* Downward stroke of V. */
		while ( this._commonMultigrid.hasCoarser() )
		{
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
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable);
				variableMultigrid.fillArrayFromFiner(CONCN, 0.5);
				variableMultigrid.fillArrayFromFiner(LOCALERROR, 0.5);
				variableMultigrid.fillArrayFromFiner(CUMULATIVEERROR, 0.5);
			}
			/* Update the PRODUCTIONRATE arrays using updated CONCN values. */
			// TODO
			
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
				currentLayer.addArrayToArray(CUMULATIVEERROR, RELATIVEERROR);
				// TODO only do this if "order+1 == outer"
				truncationError = currentLayer.getNorm(RELATIVEERROR);
				this._truncationErrors.put(variable.getName(), truncationError);
			}
		}
		
		
	}

	private void refreshCommonGrid(SpatialGrid commonGrid)
	{
		/* Make the common multigrid if this is the first time. */
		if ( this._commonMultigrid == null )
		{
			this._commonMultigrid = 
					MultigridLayer.generateCompleteMultigrid(commonGrid);
		}
		/* 
		 * Wipe all old values in the coarser layers, replacing them with the
		 * finest values.
		 */
		MultigridLayer.replaceAllLayersFromFinest(this._commonMultigrid);
	}
	
	private MultigridLayer getMultigrid(SpatialGrid variable)
	{
		String name = variable.getName();
		if ( this._multigrids.containsKey(name) )
			return this._multigrids.get(name);
		/* New variable, so we need to make the MultigridLayer. */
		MultigridLayer newMultigrid = 
				MultigridLayer.generateCompleteMultigrid(variable);
		this._multigrids.put(name, newMultigrid);
		return newMultigrid;
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
		double rhs;
		
		for ( int[] current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			if ( commonGrid.getValueAt(WELLMIXED, current) >= 
					this._boundaryThreshold )
			{
				continue;
			}
			
			
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
