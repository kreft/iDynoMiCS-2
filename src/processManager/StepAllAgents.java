package processManager;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;


public class StepAllAgents extends ProcessManager
{
	public StepAllAgents()
	{
		
	}
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.doActivity("grow", this._timeStepSize);
			agent.doActivity("divide", null);
			agent.doActivity("die", null);
		}
	}
}