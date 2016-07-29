package solver;

import java.util.Collection;
import java.util.LinkedList;

import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import static grid.ArrayType.*;

import grid.ArrayType;
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
	 * \brief TODO
	 * 
	 * @param solute
	 * @param commonGrid
	 */
	public void solveToConvergence(SpatialGrid solute, SpatialGrid commonGrid)
	{
		/*
		 * 
		 */
		double tStep = 1.0;
		double maxAbsDiff = 1.0E-3;
		int maxIter = 1000;
		/*
		 * 
		 */
		double[][][] oldArray = solute.getArray(ArrayType.CONCN);
		double absDiff = Double.MAX_VALUE;
		/* Package the single variable grid into a collection. */
		Collection<SpatialGrid> variables = new LinkedList<SpatialGrid>();
		variables.add(solute);
		for ( int i = 0; i < maxIter && absDiff > maxAbsDiff; i++ )
		{
			this.solve(variables, commonGrid, tStep);
			absDiff = solute.getTotalAbsDiffWith(oldArray, ArrayType.CONCN);
		}
	}
	
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
	protected void applyDiffusion(SpatialGrid grid, SpatialGrid commonGrid)
	{
		Tier level = BULK;
		Shape shape = grid.getShape();
		/* Coordinates of the current position. */
		int[] current, nhb;
		/* Temporary storage. */
		double totalFlux, nbhFlux;
		/*
		 * Iterate over all core voxels calculating the Laplace operator. 
		 */
		for ( current = shape.resetIterator(); shape.isIteratorValid();
											  current = shape.iteratorNext())
		{
			// TODO this should really be > some threshold
			if ( commonGrid.getValueAt(WELLMIXED, current) == 1.0 )
				continue;
			totalFlux = 0.0;
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
				/*
				 * If the neighbouring voxel is in a boundary, the boundary may
				 * take a note of the flux (for connections with other
				 * compartments).
				 */
				nbhFlux = grid.getDiffusionFromNeighbor();
				
				/*
				 * If this flux came from a well-mixed voxel, inform the grid.
				 * Alternatively, if it came from a well-mixed boundary, inform
				 * the grid.
				 */
				if ( shape.isNbhIteratorInside() )
				{
					// TODO this should really be > some threshold
					if ( commonGrid != null && 
							commonGrid.getValueAt(WELLMIXED, nhb) == 1.0 )
					{
						this.increaseWellMixedFlow(grid.getName(), - nbhFlux);
					}
				}
				else if ( shape.isNbhIteratorValid() )
				{
					if ( shape.nbhIteratorOutside().needsToUpdateWellMixed() )
						this.increaseWellMixedFlow(grid.getName(), - nbhFlux);
				}
				totalFlux += nbhFlux;
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
								" contributes flux of "+nbhFlux);
					}
					else
					{
						Log.out(level, 
								" boundary nhb "+Vector.toString(nhb)
								+ " contributes flux of "+nbhFlux);
					}
				}
			}
			/*
			 * Flux is in units of mass/mole per unit time. Divide by the voxel
			 * volume to convert this to a rate of change in concentration.
			 */
			double volume = shape.getCurrVoxelVolume();
			double changeRate = totalFlux / volume;
			/*
			 * Finally, apply this to the relevant array.
			 */
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, " Total flux = "+totalFlux);
				Log.out(level, " Voxel volume = "+volume);
				Log.out(level, " Total rate of change from flux = "+changeRate);
			}
			grid.addValueAt(LOPERATOR, current, changeRate);
		}
	}
	
	/* ***********************************************************************
	 * WELL-MIXED CHANGES
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @return
	 */
	protected abstract double getWellMixedFlow(String name);
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param flow
	 */
	protected abstract void increaseWellMixedFlow(String name, double flow);
}