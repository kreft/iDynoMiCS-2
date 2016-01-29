/**
 * 
 */
package grid.domainSetter;

import grid.SpatialGrid;
import idynomics.AgentContainer;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @since January 2016
 */
public interface IsDomainSetter
{
	public void updateDomain(SpatialGrid aGrid, AgentContainer agents);
}