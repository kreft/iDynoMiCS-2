package processManager;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

public class AgentStochasticMove extends ProcessManager {


	protected void internalStep(EnvironmentContainer environment, AgentContainer agents) {
		for ( Agent agent : agents.getAllLocatedAgents() )
		{
			agent.event("stochasticMove", _timeStepSize);
		}
	}
	

}
