/**
 * 
 */
package processManager;

import agent.Agent;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

/**
 * 
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) 
 */
public class PrepareSoluteGrids extends ProcessManager
{

	/**
	 * \brief TODO
	 * 
	 */
	public PrepareSoluteGrids()
	{
		
	}
	
	/**
	 * \brief TODO
	 * 
	 * 
	 * 
	 */
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/*
		 * Reset each solute grid's relevant arrays.
		 */
		for ( String sName : environment.getSoluteNames() )
		{
			environment.getSoluteGrid(sName).newArray(SpatialGrid.reac);
			environment.getSoluteGrid(sName).newArray(SpatialGrid.dReac);
			environment.getSoluteGrid(sName).newArray(SpatialGrid.domain);
			environment.getSoluteGrid(sName).newArray(SpatialGrid.diff);
		}
		/*
		 * Iterate through the agents, asking them to apply the relevant
		 * information.
		 */
		for ( Agent agent : agents.getAllLocatedAgents() )
		{
			// TODO Give agent solute grids; agent updates reac rates, 
			// diffReac, domain... diffusivity?
		}
		
		// TODO update domain to include boundary layer
		
		// TODO reaction rates not catalysed by agents
	}

}
