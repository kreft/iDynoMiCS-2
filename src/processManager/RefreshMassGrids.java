package processManager;

import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import agent.Agent;

public class RefreshMassGrids extends ProcessManager
{
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		//FIXME NULL the agent mass grids here!!!!
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.event("massToGrid");
		}
	}
}