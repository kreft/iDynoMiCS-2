package solver;

import java.util.HashMap;
import java.util.function.Consumer;

import grid.SpatialGrid;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public abstract class PDEsolver extends Solver
{
	/**
	 * TODO
	 */
	protected Consumer<HashMap<String, SpatialGrid>> _updaterFunction;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public PDEsolver()
	{
		
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param f
	 */
	public void setUpdaterFunc(Consumer<HashMap<String, SpatialGrid>> f)
	{
		this._updaterFunction = f;
	}
	
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @param tFinal
	 */
	public abstract void solve(HashMap<String, SpatialGrid> solutes,
															double tFinal);
	
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * <p>Requires the arrays "domain", "diffusivity" and "concentration" to
	 * be pre-filled in <b>solute</b>.</p>
	 * 
	 * @param solute
	 * @param arrayName
	 */
	protected void addLOperator(SpatialGrid solute, String arrayName)
	{
		/*
		 * Reset the SpatialGrid's L-Operator array.
		 */
		solute.newArray(arrayName);
		/*
		 * Solute concentration and diffusivity at the current grid 
		 * coordinates.
		 */
		double currConcn, currDiff;
		/*
		 * Solute concentration and diffusivity in the neighboring voxels;
		 */
		Double[][] concnNbh, diffNbh;
		/*
		 * Temporary storage for the L-Operator.
		 */
		double lop;
		/*
		 * Iterate over all core voxels calculating the L-Operator. 
		 */
		for (int[] current = solute.resetIterator(); solute.isIteratorValid();
											  current = solute.iteratorNext())
		{
			if ( solute.getValueAt(SpatialGrid.domain, current) == 0.0 )
				continue;
			currConcn = solute.getValueAt(SpatialGrid.concn, current);
			currDiff = solute.getValueAt(SpatialGrid.diff, current);
			concnNbh = solute.getNeighborValues(SpatialGrid.concn, current);
			diffNbh = solute.getNeighborValues(SpatialGrid.diff, current);
			lop = 0.0;
			for ( int axis = 0; axis < 3; axis++ )
				for ( int i = 0; i < 2; i++ )
				{
					try {	lop += (diffNbh[axis][i] + currDiff) *
											(concnNbh[axis][i] - currConcn); }
					catch (Exception e) {}
				}
			/*
			 * Here we assume that all voxels are the same size.
			 */
			lop *= 0.5 / Math.pow(solute.getResolution(), 2.0);
			lop += solute.getValueAt(SpatialGrid.reac, current);
			solute.addValueAt(arrayName, current, lop);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>Requires the arrays "domain", "diffusivity" and "diffReac" to be
	 * pre-filled in <b>solute</b>.</p>
	 * 
	 * @param solute 
	 * @param arrayName
	 */
	protected void divideByDiffLOperator(SpatialGrid solute, String arrayName)
	{
		/*
		 * Reset the SpatialGrid's array
		 * TODO skip this?
		 */
		solute.newArray(arrayName);
		
		/*
		 * Diffusivity at the current grid coordinates.
		 */
		double currDiff;
		/*
		 * Diffusivity in the neighboring voxels;
		 */
		Double[][] diffNbh;
		/*
		 * Temporary storage for the derivative of the L-Operator.
		 */
		double dLop;
		/*
		 * Iterate over all core voxels calculating the derivative of the 
		 * L-Operator. 
		 */
		for (int[] current = solute.resetIterator(); solute.isIteratorValid();
											  current = solute.iteratorNext())
		{
			if ( solute.getValueAt(SpatialGrid.domain, current) == 0.0 )
				continue;
			currDiff = solute.getValueAt(SpatialGrid.diff, current);
			diffNbh = solute.getNeighborValues(SpatialGrid.diff, current);
			dLop = 0.0;
			for ( int axis = 0; axis < 3; axis++ )
				for ( int i = 0; i < 2; i++ )
				{
					try {	dLop += (diffNbh[axis][i] + currDiff); }
					catch (ArrayIndexOutOfBoundsException e) {}
				}
			/*
			 * Here we assume that all voxels are the same size.
			 */
			dLop *= 0.5 / Math.pow(solute.getResolution(), 2.0);
			dLop += solute.getValueAt(SpatialGrid.dReac, current);
			solute.timesValueAt(arrayName, current, 1.0/dLop);
		}
	}
}
