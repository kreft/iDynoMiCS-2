/**
 * 
 */
package solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import grid.SpatialGrid;

/**
 * \brief: Placeholder solver for scenarios with static concentration
 * 
 * not a PDE solver, but a dummy that only enables agent-reactions, environment
 * remains unchanged. Useful for testing and debugging.
 * 
 * @author Bastiaan
 *
 */
public class PDEagentsOnly extends PDEsolver
{

	protected Map<String,Double> _wellMixedChanges = 
			new HashMap<String,Double>();
	
	public PDEagentsOnly()
	{
		
	}
	
	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		double dt = tFinal;
		this._updater.prestep(variables, dt);
	}
	
	/* ***********************************************************************
	 * WELL-MIXED CHANGES
	 * **********************************************************************/
	
	@Override
	protected double getWellMixedFlow(String name)
	{
		return 0.0;
	}
	
	@Override
	protected void increaseWellMixedFlow(String name, double flow)
	{

	}
	
	protected void resetWellMixedFlow(String name)
	{

	}

	@Override
	public void setAbsoluteTolerance(double tol) {
	}

	@Override
	public void setRelativeTolerance(double tol) {
	}
}
