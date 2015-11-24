package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.State;

public class DimensionsBoundingBox implements State {

	public void init(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get("body")).dimensions((double) agent.get("radius"));
	}

}
