package solver;

import java.util.Arrays;
import java.util.HashMap;

import dataIO.Log;
import static dataIO.Log.Tier.*;
import grid.SpatialGrid;
import static grid.SpatialGrid.ArrayType.*;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class PDEsolver extends Solver
{
	/**
	 * TODO
	 */
	protected PDEupdater _updater;
	
	/**
	 * \brief TODO
	 * 
	 * @param updater
	 */
	public void setUpdater(PDEupdater updater)
	{
		this._updater = updater;
	}
	
	/*************************************************************************
	 * SOLVER METHODS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @param tFinal
	 */
	public abstract void solve(HashMap<String, SpatialGrid> solutes,
															double tFinal);
	
	/**
	 * \brief Add the Laplacian Operator to the LOPERATOR array of the given
	 * grid.
	 * 
	 * <p>The Laplacian Operator is the divergence of the gradient of the
	 * concentration, and is commonly denoted by ∆ (capital delta) or
	 * nabla<sup>2</sup>.</p>
	 * 
	 * <p>Requires the arrays "domain", "diffusivity" and "concentration" to
	 * be pre-filled in <b>solute</b>.</p>
	 * 
	 * @param varName
	 * @param grid
	 * @param destType
	 */
	protected void addFluxes(String varName, SpatialGrid grid)
	{
		/* Coordinates of the current position. */
		int[] current;
		/* Temporary storage. */
		double flux;
		/*
		 * Iterate over all core voxels calculating the Laplace operator. 
		 */
		for ( current = grid.resetIterator(); grid.isIteratorValid();
											  current = grid.iteratorNext())
		{
			if ( grid.getValueAt(WELLMIXED, current) == 0.0 )
				continue;
			flux = 0.0;
			for ( grid.resetNbhIterator(); 
						grid.isNbhIteratorValid(); grid.nbhIteratorNext() )
			{
				flux += grid.getFluxWithNeighbor(varName);
			}
			/*
			 * Finally, apply this to the relevant array.
			 */
			Log.out(BULK, Arrays.toString(current)+": val = "+
							grid.getValueAtCurrent(CONCN)+", lop = "+flux);
			grid.addValueAt(LOPERATOR, current, flux);
		}
	}
}