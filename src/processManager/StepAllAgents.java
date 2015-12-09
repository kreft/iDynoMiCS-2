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
			agent.eventTrigger("grow", null, this._timeStepSize);
			agent.eventTrigger("divide", null, null);
			agent.eventTrigger("die", null, null);
		}
	}
}