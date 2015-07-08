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
			agent.step(this._timeStepSize, solutes);
	}
}
