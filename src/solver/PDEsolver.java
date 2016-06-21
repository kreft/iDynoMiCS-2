package solver;

import java.util.Collection;
import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import static grid.ArrayType.*;

import grid.SpatialGrid;
import linearAlgebra.Vector;
import shape.Shape;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
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
	
	/* ***********************************************************************
	 * SOLVER METHODS
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @param commonGrid TODO
	 * @param tFinal
	 */
	public abstract void solve(Collection<SpatialGrid> solutes,
			SpatialGrid commonGrid, double tFinal);
	
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
	 * @param commonGrid
	 * @param destType
	 */
	protected void addFluxes(SpatialGrid grid, SpatialGrid commonGrid)
	{
		Tier level = BULK;
		Shape shape = grid.getShape();
		/* Coordinates of the current position. */
		int[] current, nhb;
		/* Temporary storage. */
		double flux, temp;
		/*
		 * Iterate over all core voxels calculating the Laplace operator. 
		 */
		for ( current = shape.resetIterator(); shape.isIteratorValid();
											  current = shape.iteratorNext())
		{
			// TODO this should really be > some threshold
			if ( commonGrid.getValueAt(WELLMIXED, current) == 1.0 )
				continue;
			flux = 0.0;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, 
						"Coord "+Vector.toString(shape.iteratorCurrent())+
						" (curent value "+grid.getValueAtCurrent(CONCN)+
						"): calculating flux...");
			}
			for ( nhb = shape.resetNbhIterator(); shape.isNbhIteratorValid();
					nhb = shape.nbhIteratorNext() )
			{
				temp = grid.getFluxFromNeighbor();
				// TODO this should really be > some threshold
				/*
				 * If this flux came from a well-mixed voxel, inform the grid.
				 */
				if ( commonGrid.getValueAt(WELLMIXED, nhb) == 1.0 )
					grid.increaseWellMixedFlux( - flux );
				flux += temp;
				/* 
				 * To get the value we must be inside, the flux can be obtained
				 * from boundary.
				 */
				if ( Log.shouldWrite(level) )
				{
					if ( shape.isNhbIteratorInside() )
					{

						Log.out(level, 
								"   nhb "+Vector.toString(nhb)+
								" ("+grid.getValueAtNhb(CONCN)+") "+
								" contributes flux of "+temp);
					}
					else
					{
						Log.out(level, 
								" boundary nhb "+Vector.toString(nhb)
								+ " contributes flux of "+temp);
					}
				}
			}
			/*
			 * Finally, apply this to the relevant array.
			 */
			if ( Log.shouldWrite(level) )
				Log.out(level, " TOTAL flux = "+flux);
			grid.addValueAt(LOPERATOR, current, flux);
		}
	}
}