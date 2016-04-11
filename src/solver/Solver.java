/**
 * 
 */
package solver;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public int nVar()
	{
		return this._variableNames == null ? 0 : this._variableNames.length;
	}
}