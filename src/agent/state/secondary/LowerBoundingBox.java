package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.State;

public class LowerBoundingBox implements State {

	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get("body")).coord((double) agent.get("radius"));
	}

}
