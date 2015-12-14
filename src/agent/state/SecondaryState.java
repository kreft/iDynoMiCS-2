package agent.state;

import agent.Agent;

public abstract class SecondaryState implements State {
	
	protected String[] input;

	public void setInput(Object state)
	{
		input = ((String) state).split(",");
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