/**
 * 
 */
package solver;

import static grid.ArrayType.CHANGERATE;
import static grid.ArrayType.CONCN;
import static grid.ArrayType.DIFFUSIVITY;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import grid.SpatialGrid;

/**
 * \brief TODO
 * 
 * FIXME: please include a small description, what kind of pde solver is used
 * here? Is it a known and documented algorithm?
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
	 * Maximal time step is the inverse of the maximal diffusivity times the
	 * maximal flux potential times the number of dimensions.
	 * 
	 * TODO Rob[23Feb2016]: Jan has suggested that we check for variables
	 * converging and then focus on the variables that are still changing.
	 */
	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;

		int nIter = 1;
		for ( SpatialGrid var : variables )
		{
			double inverseMaxT = var.getMax(DIFFUSIVITY);
			inverseMaxT *= var.getShape().getMaxFluxPotential();
			inverseMaxT *= var.getShape().getNumberOfDimensions() * 2.0;
			// FIXME testing why solute concentrations explode sometimes
			// FIXME decreasing time step  a bit further seems to fix exploding solute concentrations
			/* divide by 3 since all simulations are pseudo 3D */
			dt =  Math.min(dt, 1.0 / inverseMaxT);

		}
		/* If the mini-timestep is less than tFinal, split it up evenly. */
		if ( dt < tFinal )
		{
			nIter = (int) Math.ceil(tFinal/dt);
			dt = tFinal/nIter;
		}
		/*
		 * Iterate over all mini-timesteps.
		 */
		for ( int iter = 0; iter < nIter; iter++ )
		{
			/* Update reaction rates, etc. */
			this._updater.prestep(variables, dt);
			for ( SpatialGrid var : variables )
			{
				var.newArray(CHANGERATE);
				this.applyDiffusion(var, commonGrid);
				var.addArrayToArray(CHANGERATE, PRODUCTIONRATE);
				var.timesAll(CHANGERATE, dt);
				var.addArrayToArray(CONCN, CHANGERATE);
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

	@Override
	public void setAbsoluteTolerance(double tol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRelativeTolerance(double tol) {
		// TODO Auto-generated method stub
		
	}
}
