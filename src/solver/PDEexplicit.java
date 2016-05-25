/**
 * 
 */
package solver;

import java.util.HashMap;

import dataIO.Log;

import static dataIO.Log.Tier.*;
import grid.SpatialGrid;
import shape.Shape;

import static grid.SpatialGrid.ArrayType.*;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	 * <p>Requires the arrays "diffusivity" and "concentration" to
	 * be pre-filled in each SpatialGrid.</p>
	 * 
	 * <p><b>[Rob 13Aug2015]</b> Time step is at most 10% of dx<sup>2</sup>/D,
	 * as this works well in tests.</p>
	 * 
	 * 
	 * TODO Rob[23Feb2016]: Jan has suggested that we check for variables
	 * converging and then focus on the variables that are still changing.
	 */
	@Override
	public void solve(HashMap<String, SpatialGrid> variables, double tFinal)
	{
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;
		Log.out(DEBUG, "PDEexplicit starting with ministep size "+dt);
		SpatialGrid var;
		int nIter = 1;
		for ( String varName : this._variableNames )
		{
			var = variables.get(varName);
			dt = Math.min(dt, 0.1 * var.getShape().getMaxFluxPotential()  /
					 var.getMin(DIFFUSIVITY));
			Log.out(DEBUG, "PDEexplicit: variable \""+varName+
					"\" has min flux "+var.getShape().getMaxFluxPotential() +
					" and diffusivity "+var.getMin(DIFFUSIVITY));
		}
		/* If the mini-timestep is less than tFinal, split it up evenly. */
		if ( dt < tFinal )
		{
			nIter = (int) Math.ceil(tFinal/dt);
			dt = tFinal/nIter;
		}
		Log.out(DEBUG, "PDEexplicit using ministep size "+dt);
		/*
		 * Iterate over all mini-timesteps.
		 */
		for ( int iter = 0; iter < nIter; iter++ )
		{
			Log.out(BULK, "Ministep "+iter+": "+(iter+1)*dt);
			this._updater.prestep(variables, dt);
			for ( String varName : this._variableNames )
			{
				var = variables.get(varName);
				var.newArray(LOPERATOR);
				this.addFluxes(varName, var);
				Log.out(BULK, "Total value of fluxes: "+
						var.getTotal(PRODUCTIONRATE));
				Log.out(BULK, "Total value of production rate array: "+
						var.getTotal(PRODUCTIONRATE));
				var.addArrayToArray(LOPERATOR, PRODUCTIONRATE);
				var.timesAll(LOPERATOR, dt);
				var.addArrayToArray(CONCN, LOPERATOR);
				if ( ! this._allowNegatives )
					var.makeNonnegative(CONCN);
			}
		}
	}
	
	public void solve(Shape aShape, HashMap<String, 
										SpatialGrid> variables, double tFinal)
	{
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;
		Log.out(DEBUG, "PDEexplicit starting with ministep size "+dt);
		SpatialGrid var;
		int nIter = 1;
		
		double minDiffusivity = Double.MAX_VALUE;
		for ( String varName : this._variableNames )
		{
			var = variables.get(varName);
			minDiffusivity = Math.min(minDiffusivity, var.getMin(DIFFUSIVITY));
			Log.out(DEBUG, "PDEexplicit: variable \""+varName+
							"\" has min diffusivity "+var.getMin(DIFFUSIVITY));
		}
		dt = 0.1 * aShape.getMaxFluxPotential() / minDiffusivity;
		/* If the mini-timestep is less than tFinal, split it up evenly. */
		if ( dt < tFinal )
		{
			nIter = (int) Math.ceil(tFinal/dt);
			dt = tFinal/nIter;
		}
		Log.out(DEBUG, "PDEexplicit using ministep size "+dt);
		/*
		 * Iterate over all mini-timesteps.
		 */
		for ( int iter = 0; iter < nIter; iter++ )
		{
			Log.out(BULK, "Ministep "+iter+": "+(iter+1)*dt);
			this._updater.prestep(variables, dt);
			for ( String varName : this._variableNames )
			{
				var = variables.get(varName);
				var.newArray(LOPERATOR);
				addFluxes(varName, var);
				var.addArrayToArray(LOPERATOR, PRODUCTIONRATE);
				var.timesAll(LOPERATOR, dt);
				var.addArrayToArray(CONCN, LOPERATOR);
				if ( ! this._allowNegatives )
					var.makeNonnegative(CONCN);
			}
		}
	}
}
