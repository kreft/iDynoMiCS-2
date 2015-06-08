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
		{
			agent.doActivity("grow", this._timeStepSize);
			agent.doActivity("divide", null);
			agent.doActivity("die", null);
		}
	}
}
