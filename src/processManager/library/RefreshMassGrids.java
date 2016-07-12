package processManager.library;

import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import agent.Agent;
import grid.ArrayType;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class RefreshMassGrids extends ProcessManager
{
	
	public static String MASS_TO_GRID = AspectRef.massToGrid;
	public static String BIOMASS = AspectRef.biomass;
	
	@Override
	protected void internalStep()
	{
		//FIXME: reset biomass for testing purpose, needs to be done properly
		this._environment.getSoluteGrid(BIOMASS).setAllTo(ArrayType.CONCN, 0.0);
		// FIXME: does massToGrid deserve a place in NameRef?
		for ( Agent agent : this._agents.getAllAgents() )
			agent.event(MASS_TO_GRID);
	}
}