package agent.event.library;

import agent.Agent;
import agent.Body;
import agent.event.Event;
import generalInterfaces.AspectInterface;

public class UpdateBody extends Event {

	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		Body body = (Body) agent.get(input[0]);
		body.update((double) agent.get(input[1]), 0.0);
	}
}