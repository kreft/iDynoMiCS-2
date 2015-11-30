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
		
		Event growth = new SimpleGrowth();
		Event divide = new CoccoidDivision();
		for ( Agent agent : agents.getAllAgents() )
		{
			growth.start(agent, null, _timeStepSize);
			divide.start(agent, null, _timeStepSize);
		}
	}
	

}
