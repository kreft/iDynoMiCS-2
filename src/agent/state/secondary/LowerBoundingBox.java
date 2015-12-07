package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.SecondaryState;
import agent.state.State;

public class LowerBoundingBox extends SecondaryState {
	
	////////////////////////////////////////////////
	// input index
	// 0: body
	// 1: radius
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get(input[0])).coord((double) agent.get(input[1]));
	}

}
