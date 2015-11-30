package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.State;

public class JointsState implements State {

	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get("body")).getJoints();
	}
	
	public State copy()
	{
		return this;
	}
}
