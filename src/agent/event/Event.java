package agent.event;

import agent.Agent;

public interface Event {
	
	public void start(Agent initiator, Agent compliant, Double timeStep);

}
