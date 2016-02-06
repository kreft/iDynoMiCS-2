package agent.event.library;

import agent.Agent;
import agent.Body;
import agent.event.Event;

public class UpdateBody extends Event {

	public void start(Agent agent, Agent compliant, Double timeStep)
	{
		Body body = (Body) agent.get(input[0]);
		body.update((double) agent.get(input[1]), 0.0);
	}
	
	/**
	 * Events are general behavior patterns, copy returns this
	 */
	public Object copy() 
	{
		return this;
	}
	
}