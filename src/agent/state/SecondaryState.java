package agent.state;

import generalInterfaces.Copyable;
import agent.Agent;

public abstract class SecondaryState implements State, Copyable {
	
	protected String[] input;

	public void setInput(String input)
	{
		this.input = input.split(",");
	}
	
	public String[] getInput()
	{
		return input;
	}
	
	public State copy()
	{
		return this;
	}
	
	public State Duplicate(Agent agent)
	{
		return this;
	}
	
	public abstract Object get(Agent agent);
}