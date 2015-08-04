/**
 * 
 */
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
public abstract class Solver
{
	/**
	 * List of solute names that this solver is responsible for.
	 */
	protected String[] _variableNames;
	
	/**
	 * TODO
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
	
	/**
	 * \brief TODO
	 * 
	 * @param variableNames
	 * @param allowNegatives
	 */
	public void init(String[] variableNames, boolean allowNegatives)
	{
		this._variableNames = variableNames;
		this._allowNegatives = allowNegatives;
	}
}