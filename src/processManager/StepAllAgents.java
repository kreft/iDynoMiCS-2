package processManager;

import java.util.HashMap;

import agent.Agent;
import grid.SpatialGrid;
import idynomics.AgentContainer;


public class StepAllAgents extends ProcessManager
{
	public StepAllAgents()
	{
		
	}
	
	@Override
	protected void internalStep(HashMap<String, SpatialGrid> solutes,
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