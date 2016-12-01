/**
 * 
 */
package grid.diffusivitySetter;

import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import instantiatable.Instantiatable;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public interface IsDiffusivitySetter extends Instantiatable
{
	/**
	 * \brief TODO
	 * 
	 * @param diffusivityGrid
	 * @param env
	 * @param agents
	 */
	public void updateDiffusivity(SpatialGrid diffusivityGrid, 
			EnvironmentContainer env, AgentContainer agents);
}