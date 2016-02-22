/**
 * 
 */
package solver;

import java.util.HashMap;

import grid.SpatialGrid;

/**
 * \brief Abstract class of the updater methods used by any {@code PDEsolver}.
 * 
 * <p>Most, if not all, {@code PDEsolver}s will divide up the timestep given to
 * solve into mini-timesteps. </p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public interface PDEupdater
{
	/**
	 * \brief Method to be applied to the variables before each mini-timestep.
	 * 
	 * <p>Examples include re-calculating reaction rates given the updated
	 * solute concentrations.</p>
	 * 
	 * @param variables Dictionary of variables, each given by its name and the
	 * {@code SpatialGrid} that contains its values over space.
	 * @param dt Size of the mini-timestep.
	 */
	default void prestep(HashMap<String, SpatialGrid> variables, double dt)
	{ }
}