/**
 * 
 */
package processManager;

import java.util.HashMap;

import agent.Agent;
import grid.SpatialGrid;
import idynomics.AgentContainer;

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
	protected void internalStep(HashMap<String, SpatialGrid> solutes,
														AgentContainer agents)
	{
		/*
		 * Reset each solute grid's relevant arrays.
		 */
		for ( String sName : solutes.keySet() )
		{
			solutes.get(sName).newArray(SpatialGrid.reac);
			solutes.get(sName).newArray(SpatialGrid.dReac);
			solutes.get(sName).newArray(SpatialGrid.domain);
			solutes.get(sName).newArray(SpatialGrid.diff);
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
