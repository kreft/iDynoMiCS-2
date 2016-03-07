package processManager;

import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import agent.Agent;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class RefreshMassGrids extends ProcessManager
{
	@Override
	protected void internalStep(
					EnvironmentContainer environment, AgentContainer agents)
	{
		//FIXME: reset biomass for testing purpose, needs to be done properly
		environment.getSoluteGrid("biomass").setAllTo(ArrayType.CONCN, 0.0);
		// FIXME: does massToGrid deserve a place in NameRef?
		for ( Agent agent : agents.getAllAgents() )
			agent.event("massToGrid");
	}
}