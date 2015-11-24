package agent.state;

import agent.Agent;

public class PrimaryState implements State {
	private Object state;

	public void set(Object state)
	{
		this.state = state;
	}
	
	public Object get(Agent agent)
	{
		return state;
	}
}
