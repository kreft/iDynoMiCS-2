package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.SecondaryState;
import agent.state.State;

public class LowerBoundingBox extends SecondaryState implements State {
	
	//input body, radius

	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get(input[0])).coord((double) agent.get(input[1]));
	}
	
	public State copy()
	{
		return this;
	}
}
