package solver;

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
		double[][] concnNbh, diffNbh;
		/*
		 * Temporary storage for the L-Operator.
		 */
		double lop;
		/*
		 * Iterate over all core voxels calculating the L-Operator. 
		 */
		solute.resetIterator();
		int[] current;
		while ( solute.iteratorHasNext() )
		{
			current = solute.iteratorNext();
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
					catch (ArrayIndexOutOfBoundsException e) {}
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
		
		int[] current;
		/*
		 * Diffusivity at the current grid coordinates.
		 */
		double currDiff;
		/*
		 * Diffusivity in the neighboring voxels;
		 */
		double[][] diffNbh;
		/*
		 * Temporary storage for the derivative of the L-Operator.
		 */
		double dLop;
		/*
		 * Iterate over all core voxels calculating the derivative of the 
		 * L-Operator. 
		 */
		solute.resetIterator();
		while ( solute.iteratorHasNext() )
		{
			current = solute.iteratorNext();
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
