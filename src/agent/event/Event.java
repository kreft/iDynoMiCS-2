package agent.event;

import agent.Agent;

public abstract class Event {
	
	protected String[] input;

	public void setInput(String input)
	{
		this.input = input.split(",");
	}
	
	public String[] getInput()
	{
		return input;
	}
	
	public abstract void start(Agent initiator, Agent compliant, Double timeStep);

}
