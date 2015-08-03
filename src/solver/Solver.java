/**
 * 
 */
package solver;

import grid.SpatialGrid;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public abstract class Solver
{
	/**
	 * List of solute names that this solver is responsible for.
	 */
	protected String[] _variableNames;
	
	/**
	 * 
	 */
	protected boolean _allowNegatives;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public Solver()
	{
		
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	public void init(String[] variableNames, boolean allowNegatives)
	{
		this._variableNames = variableNames;
		this._allowNegatives = allowNegatives;
	}
	
	
	public abstract void solve(HashMap<String, SpatialGrid> solutes,
															double tFinal);
}