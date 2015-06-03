package mechanism;

import agent.Agent;
import spatialgrid.SoluteGrid;
import idynomics.AgentContainer;

public class StepAllAgents extends Mechanism
{
	public StepAllAgents() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void step(SoluteGrid[] solutes, AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
			agent.step(this._timeStepSize, solutes);
		// TODO Auto-generated method stub
		this._timeForNextStep += this._timeStepSize;
	}

}
