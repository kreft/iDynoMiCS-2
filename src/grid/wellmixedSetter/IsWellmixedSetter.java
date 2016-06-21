/**
 * 
 */
package grid.wellmixedSetter;

import grid.SpatialGrid;
import idynomics.AgentContainer;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public interface IsWellmixedSetter
{
	/**
	 * \brief TODO
	 * 
	 * @param aGrid
	 * @param agents
	 */
	public void updateWellmixed(SpatialGrid aGrid, AgentContainer agents);
}