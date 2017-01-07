package solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import grid.ArrayType;
import grid.SpatialGrid;
import solver.multigrid.MultigridLayer;

public class PDEmultigrid extends PDEsolver
{
	private PDEgaussseidel _solver = new PDEgaussseidel();
	
	private Map<String, MultigridLayer> _multigrids = 
			new HashMap<String, MultigridLayer>();
	
	private MultigridLayer _commonMultigrid;
	
	private int _numVCycles;
	
	private int _numPreSteps;
	
	private int _numPostSteps;
	
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
					this._solver.relax(currentLayer, currentCommon, tFinal);
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
