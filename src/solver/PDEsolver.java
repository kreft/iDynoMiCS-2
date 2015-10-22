package solver;

import java.util.ArrayList;
import java.util.HashMap;

import grid.CartesianGrid;
import grid.SpatialGrid.ArrayType;

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
		default void presolve(HashMap<String, CartesianGrid> variables)
		{ }
		
		/**
		 * \brief Method to be applied to the variables before each mini time
		 * step.
		 * 
		 * @param variables
		 */
		default void prestep(HashMap<String, CartesianGrid> variables)
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
	public abstract void solve(HashMap<String, CartesianGrid> solutes,
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
	protected void addLOperator(CartesianGrid solute, ArrayType type)
	{
		/*
		 * Reset the SpatialGrid's L-Operator array.
		 */
		solute.newArray(ArrayType.LOPERATOR);
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
			if ( solute.getValueAt(ArrayType.DOMAIN, current) == 0.0 )
				continue;
			currConcn = solute.getValueAt(ArrayType.CONCN, current);
			currDiff = solute.getValueAt(ArrayType.DIFFUSIVITY, current);
			concnNbh = solute.getNeighborValues(ArrayType.CONCN, current);
			diffNbh = solute.getNeighborValues(ArrayType.DIFFUSIVITY, current);
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
			lop += solute.getValueAt(ArrayType.PRODUCTIONRATE, current);
			/*
			 * Finally, apply this to the relevant array.
			 */
			solute.addValueAt(type, current, lop);
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
	protected void divideByDiffLOperator(CartesianGrid solute, ArrayType arrayType)
	{
		/*
		 * Reset the SpatialGrid's array
		 * TODO skip this?
		 */
		solute.newArray(ArrayType.LOPERATOR);
		
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
			if ( solute.getValueAt(ArrayType.DOMAIN, current) == 0.0 )
				continue;
			currDiff = solute.getValueAt(ArrayType.DIFFUSIVITY, current);
			diffNbh = solute.getNeighborValues(ArrayType.DIFFUSIVITY, current);
			dLop = 0.0;
			for ( double diffusivity : diffNbh )
				dLop += diffusivity + currDiff;
			/*
			 * Here we assume that all voxels are the same size.
			 */
			dLop *= 0.5 / Math.pow(solute.getResolution(), 2.0);
			dLop += solute.getValueAt(ArrayType.DIFFPRODUCTIONRATE, current);
			solute.timesValueAt(arrayType, current, 1.0/dLop);
		}
	}	
}