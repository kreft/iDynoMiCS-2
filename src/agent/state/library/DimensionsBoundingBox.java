package agent.state.library;

import agent.Agent;
import agent.Body;
import agent.state.SecondaryState;
import agent.state.State;

public class DimensionsBoundingBox extends SecondaryState implements State {

	// input: body, radius
	
	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get(input[0])).dimensions((double) agent.get(input[1]));
	}
	
	public State copy()
	{
		return this;
	}
}
