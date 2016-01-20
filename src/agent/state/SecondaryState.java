package agent.state;

import agent.Agent;

public abstract class SecondaryState implements State {
	
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
	
	public abstract Object get(Agent agent);
}