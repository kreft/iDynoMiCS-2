/**
 * 
 */
package solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import static grid.ArrayType.*;

import grid.SpatialGrid;

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
