package solver;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.R_H_S;
import static grid.ArrayType.RESIDUAL;
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
		/* Downward stroke of V. */
		for ( ; this._commonMultigrid.hasCoarser();
				this._commonMultigrid = this._commonMultigrid.getCoarser() )
		{
			currentCommon = this._commonMultigrid.getGrid();
			for ( SpatialGrid variable : variables )
			{
				variableMultigrid = this.getMultigrid(variable).getCoarser();
				currentLayer = variableMultigrid.getGrid();
				// NOTE in iDyno 1 this is done in stages
				for (ArrayType type : currentLayer.getAllArrayTypes())
					variableMultigrid.fillArrayFromFiner(type, 0.5);
				for ( int i = 0; i < this._numPreSteps; i++ )
					this.relax(currentLayer, currentCommon);
				this._multigrids.put(variable.getName(), variableMultigrid);
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
			
			rhs = variable.getValueAt(R_H_S, current);
			
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
	 */
	private void computeResidual(SpatialGrid variable, SpatialGrid commonGrid)
	{
		Shape shape = variable.getShape();
		double diffusiveFlow, rateFromReactions, residual;
		for ( int[] current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			if ( commonGrid.getValueAt(WELLMIXED, current) >= 
					this._boundaryThreshold )
			{
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
			variable.setValueAt(RESIDUAL, current, residual);
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
