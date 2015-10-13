package solver;

import java.util.ArrayList;
import java.util.HashMap;

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
	 * UPDATER METHODS
	 ************************************************************************/
	
	public interface Updater
	{
		/**
		 * \brief Method to be applied to the variables before the solver
		 * starts.
		 * 
		 * @param variables
		 */
		default void presolve(HashMap<String, SpatialGrid> variables)
		{ }
		
		/**
		 * \brief Method to be applied to the variables before each mini time
		 * step.
		 * 
		 * @param variables
		 */
		default void prestep(HashMap<String, SpatialGrid> variables)
		{ }
	}
	
	/**
	 * TODO
	 */
	protected Updater _updater;
	
	/**
	 * \brief TODO
	 * 
	 * @param updater
	 */
	public void setUpdater(Updater updater)
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
		ArrayList<Double> concnNbh, diffNbh;
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
			for ( int i = 0; i < concnNbh.size(); i++ )
				lop += (diffNbh.get(i)+currDiff)*(concnNbh.get(i)-currConcn);
			/*
			 * Here we assume that all voxels are the same size.
			 */
			lop *= 0.5 * Math.pow(solute.getResolution(), -2.0);
			/*
			 * Add on any reactions.
			 */
			lop += solute.getValueAt(SpatialGrid.reac, current);
			/*
			 * Finally, apply this to the relevant array.
			 */
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
		ArrayList<Double> diffNbh;
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
			for ( double diffusivity : diffNbh )
				dLop += diffusivity + currDiff;
			/*
			 * Here we assume that all voxels are the same size.
			 */
			dLop *= 0.5 / Math.pow(solute.getResolution(), 2.0);
			dLop += solute.getValueAt(SpatialGrid.dReac, current);
			solute.timesValueAt(arrayName, current, 1.0/dLop);
		}
	}	
}