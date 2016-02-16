/**
 * 
 */
package solver;

import java.util.Arrays;
import java.util.HashMap;

import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
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
	 * <p><b>[Rob 13Aug2015]</b> Time step is at most 10% of dx<sup>2</sup>/D,
	 * as this works well in tests.</p>
	 * 
	 */
	@Override
	public void solve(HashMap<String, SpatialGrid> variables, double tFinal)
	{
		//System.out.println("PDE solver being called "); //bughunt
		this._updater.presolve(variables);
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;
		//System.out.println("Starting with ministep size "+dt); //bughunt
		SpatialGrid var;
		int nIter = 1;
		for ( String varName : this._variableNames )
		{
			var = variables.get(varName);
			dt = Math.min(dt, 0.1 * var.getMinVoxVoxResSq() /
										   var.getMin(ArrayType.DIFFUSIVITY));
			//System.out.println(varName+" ministep size "+dt); //bughunt
		}
		if ( dt < tFinal )
		{
			nIter = (int) Math.ceil(tFinal/dt);
			dt = tFinal/nIter;
		}
		/*
		 * 
		 */
//		System.out.println("Using ministep size "+dt); //bughunt
		for ( int iter = 0; iter < nIter; iter++ )
		{
			//System.out.println("Ministep "+iter+": "+(iter+1)*dt); //bughunt
			this._updater.prestep(variables);
			for ( String varName : this._variableNames )
			{
				var = variables.get(varName);
				var.newArray(ArrayType.LOPERATOR);
				addLOperator(varName, var);
				var.timesAll(ArrayType.LOPERATOR, dt);
				var.addArrayToArray(ArrayType.CONCN, ArrayType.LOPERATOR);
			}
		}
	}
}
