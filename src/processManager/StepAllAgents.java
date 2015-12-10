package processManager;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;


public class StepAllAgents extends ProcessManager
{
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.action("grow", this._timeStepSize);
			agent.action("divide");
			agent.action("die");
		}
	}
}