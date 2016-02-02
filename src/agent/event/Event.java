package agent.event;

import generalInterfaces.Copyable;
import agent.Agent;
import agent.state.State;

public abstract class Event implements Copyable {
	
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

	public abstract Object copy();
	
}
