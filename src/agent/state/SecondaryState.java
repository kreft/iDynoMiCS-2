package agent.state;

import agent.Agent;

public abstract class SecondaryState implements State {
	
	protected String[] input;

	public void set(Object state)
	{
		input = ((String) state).split(",");
	}
	
	public State copy()
	{
		return this;
	}
	
	public abstract Object get(Agent agent);
}