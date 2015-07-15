package processManager;

import agent.Agent;
import spatialGrid.SoluteGrid;
import idynomics.AgentContainer;


public class StepAllAgents extends ProcessManager
{
	public StepAllAgents()
	{
		
	}
	
	@Override
	protected void internalStep(SoluteGrid[] solutes, AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.doActivity("grow", this._timeStepSize);
			agent.doActivity("divide", null);
			agent.doActivity("die", null);
		}
	}
}