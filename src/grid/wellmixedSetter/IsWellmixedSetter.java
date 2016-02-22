/**
 * 
 */
package grid.wellmixedSetter;

import grid.SpatialGrid;
import idynomics.AgentContainer;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @since January 2016
 */
public interface IsWellmixedSetter
{
	public void updateWellmixed(SpatialGrid aGrid, AgentContainer agents);
}