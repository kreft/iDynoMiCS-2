package processManager;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import agent.event.Event;
import agent.event.library.*;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

public class AgentGrowth extends ProcessManager {

	
	protected void internalStep(EnvironmentContainer environment,
											AgentContainer agents) {
		

		for ( Agent agent : agents.getAllAgents() )
		{
			agent.action("growth", _timeStepSize);
			agent.action("divide", _timeStepSize);
		}
	}
	

}
