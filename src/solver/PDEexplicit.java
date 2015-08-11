/**
 * 
 */
package solver;

import grid.SpatialGrid;

import java.util.HashMap;

import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public class PDEexplicit extends PDEsolver
{
	/**
	 * \brief TODO
	 * 
	 */
	public PDEexplicit()
	{
		
	}
	
	
	
	/**
	 * 
	 * <p>Requires the arrays "diffusivity" and "concentration" to
	 * be pre-filled in each SpatialGrid.</p>
	 * 
	 */
	@Override
	public void solve(HashMap<String, SpatialGrid> variables, double tFinal)
	{
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = Double.MAX_VALUE;
		SpatialGrid var;
		for ( String varName : this._variableNames )
		{
			var = variables.get(varName);
			dt = Math.min(dt, 0.5 * ExtraMath.sq(var.getResolution()) /
												var.getMin(SpatialGrid.diff));
		}
		int nIter = (int) Math.ceil(tFinal/dt);
		dt = tFinal/nIter;
		/*
		 * 
		 */
		for ( int iter = 0; iter < nIter; iter++ )
			for ( String varName : this._variableNames )
			{
				var = variables.get(varName);
				var.newArray("lop");
				addLOperator(var, "lop");
				var.timesAll("lop", dt, false);
				var.addArrayToArray(SpatialGrid.concn, "lop", false);
			}
	}
	
	protected void solve(SpatialGrid solute, double tFinal)
	{
		solute.newArray("next");
		
		
	}
	
}
