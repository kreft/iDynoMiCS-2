package processManager.library;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;


public class StepAllAgents extends ProcessManager
{
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.event("grow", this._timeStepSize);
			agent.event("divide");
			agent.event("die");
		}
	}
}