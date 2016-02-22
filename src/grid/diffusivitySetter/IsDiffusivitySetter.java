/**
 * 
 */
package grid.diffusivitySetter;

import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

/**
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public interface IsDiffusivitySetter
{
	/**
	 * \brief TODO
	 * 
	 * @param diffusivityGrid
	 * @param concentrationGrids
	 * @param env
	 * @param agents
	 */
	public void updateDiffusivity(SpatialGrid diffusivityGrid, SpatialGrid[] 
			concentrationGrids, EnvironmentContainer env, AgentContainer agents);
}