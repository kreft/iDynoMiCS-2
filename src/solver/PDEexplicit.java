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

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEexplicit extends PDEsolver
{
	/**
	 * 
	 */
	protected Map<String,Double> _wellMixedChanges = 
			new HashMap<String,Double>();
	
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
	 * Maximal time step is the inverse of the maximal diffusivity times the maximal
	 * flux potential times the number of dimensions.
	 * 
	 * TODO Rob[23Feb2016]: Jan has suggested that we check for variables
	 * converging and then focus on the variables that are still changing.
	 */
	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		Tier level = DEBUG;
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;
		if ( Log.shouldWrite(level) )
			Log.out(level, "PDEexplicit starting with ministep size "+dt);
		int nIter = 1;
		for ( SpatialGrid var : variables )
		{
			double inverseMaxT = var.getMax(DIFFUSIVITY);
			inverseMaxT *= var.getShape().getMaxFluxPotential();
			inverseMaxT *= var.getShape().getNumberOfDimensions();
			dt =  Math.min(dt, 1 / inverseMaxT);
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "PDEexplicit: variable \""+var.getName()+ "\" has"
					+ " max flux potential "+var.getShape().getMaxFluxPotential() +
					" and diffusivity "+var.getMin(DIFFUSIVITY));
			}
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, "PDEexplicit adjusted ministep size to "+dt);
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
				this.applyDiffusion(var, commonGrid);
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
		/*
		 * Now scale the well-mixed flow rates and apply them to the grid.
		 * Here, we can simply divide by the number of iterations, since they
		 * were all of equal time length.
		 */
		double totalFlow, scaledFlow;
		for ( SpatialGrid var : variables )
		{
			totalFlow = this.getWellMixedFlow(var.getName());
			scaledFlow = totalFlow / nIter;
			var.increaseWellMixedMassFlow(scaledFlow);
			this.resetWellMixedFlow(var.getName());
		}
	}
	
	/* ***********************************************************************
	 * WELL-MIXED CHANGES
	 * **********************************************************************/
	
	@Override
	protected double getWellMixedFlow(String name)
	{
		if ( this._wellMixedChanges.containsKey(name) )
			return this._wellMixedChanges.get(name);
		return 0.0;
	}
	
	@Override
	protected void increaseWellMixedFlow(String name, double flow)
	{
		this._wellMixedChanges.put(name, flow + this.getWellMixedFlow(name));
	}
	
	/**
	 * \brief Reset the well-mixed flow tally for the given variable.
	 * 
	 * @param name Variable name.
	 */
	protected void resetWellMixedFlow(String name)
	{
		this._wellMixedChanges.put(name, 0.0);
	}
}
