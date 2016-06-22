/**
 * 
 */
package solver;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;

import static dataIO.Log.Tier.*;
import static grid.ArrayType.*;

import grid.SpatialGrid;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
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
	
	/*
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
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		Tier level = BULK;
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;
		if ( Log.shouldWrite(level) )
			Log.out(level, "PDEexplicit starting with ministep size "+dt);
		int nIter = 1;
		for ( SpatialGrid var : variables )
		{
			dt = Math.min(dt, 0.1 * var.getShape().getMaxFluxPotential()  /
					var.getMin(DIFFUSIVITY));
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "PDEexplicit: variable \""+var.getName()+
					"\" has min flux "+var.getShape().getMaxFluxPotential() +
					" and diffusivity "+var.getMin(DIFFUSIVITY));
			}
		}
		/* If the mini-timestep is less than tFinal, split it up evenly. */
		if ( dt < tFinal )
		{
			nIter = (int) Math.ceil(tFinal/dt);
			dt = tFinal/nIter;
		}
		Log.out(level, "PDEexplicit using ministep size "+dt);
		/*
		 * Iterate over all mini-timesteps.
		 */
		for ( int iter = 0; iter < nIter; iter++ )
		{
			Log.out(level, "Ministep "+iter+": "+(iter+1)*dt);
			this._updater.prestep(variables, dt);
			for ( SpatialGrid var : variables )
			{
				if ( Log.shouldWrite(level) )
					Log.out(level, " Variable: "+var.getName());
				var.newArray(LOPERATOR);
				this.addFluxRates(var, commonGrid);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "  Total value of fluxes: "+
							var.getTotal(LOPERATOR));
					Log.out(level, "  Total value of production rate array: "+
							var.getTotal(PRODUCTIONRATE));
				}
				var.addArrayToArray(LOPERATOR, PRODUCTIONRATE);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "  Change rates: \n"+
						var.arrayAsText(LOPERATOR));
				}
				var.timesAll(LOPERATOR, dt);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "  Changes: \n"+
						var.arrayAsText(LOPERATOR));
				}
				var.addArrayToArray(CONCN, LOPERATOR);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "  Concn: \n"+
						var.arrayAsText(LOPERATOR));
				}
				if ( ! this._allowNegatives )
					var.makeNonnegative(CONCN);
			}
		}
	}
}
