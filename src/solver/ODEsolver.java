/**
 * 
 */
package solver;

import java.util.HashMap;

import grid.SpatialGrid;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @
 * @since August 2015
 */
public abstract class ODEsolver extends Solver 
{
	/**
	 * Number of variables in the solver.
	 */
	protected int _nVar;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public ODEsolver()
	{
		
	}
	
	public void init(String[] variableNames, boolean allowNegatives)
	{
		this._nVar = variableNames.length;
	}
	
	/*************************************************************************
	 * KEY METHODS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @param tFinal
	 */
	@Override
	public void solve(HashMap<String, SpatialGrid> solutes, double tFinal)
	{
		double[] y = Vector.zerosDbl(this._nVar);
		/*
		 * 
		 */
		for ( int i = 0; i < this._nVar; i++ )
		{
			y[i] = 
				solutes.get(this._variableNames[i]).getMax(SpatialGrid.concn);
		}
			
		/*
		 * 
		 */
		y = this.solve(y, tFinal);
		/*
		 * 
		 */
		for ( int i = 0; i < this._nVar; i++ )
		{
			solutes.get(this._variableNames[i]).setAllTo(SpatialGrid.concn, 
																y[i], true);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>Alternate method to solve(). Had to change the name to avoid
	 * problems with "erasure", whatever that is!</p>
	 * 
	 * @param variables
	 * @param tFinal
	 */
	public void solveDbl(HashMap<String, Double> variables, double tFinal)
	{
		double[] y = Vector.zerosDbl(this._nVar);
		/*
		 * 
		 */
		for ( int i = 0; i < this._nVar; i++ )
			y[i] = variables.get(this._variableNames[i]);
		/*
		 * 
		 */
		y = this.solve(y, tFinal);
		/*
		 * 
		 */
		for ( int i = 0; i < this._nVar; i++ )
			variables.put(this._variableNames[i], y[i]);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param y
	 * @param tFinal
	 * @return
	 */
	protected abstract double[] solve(double[] y, double tFinal);
	
	
}
