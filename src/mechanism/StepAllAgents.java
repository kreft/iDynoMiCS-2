package mechanism;

import agent.Agent;
import spatialgrid.SoluteGrid;
import idynomics.AgentContainer;

public class StepAllAgents extends Mechanism
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
