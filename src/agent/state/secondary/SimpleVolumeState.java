package agent.state.secondary;

import agent.Agent;
import agent.state.State;

public class SimpleVolumeState implements State {

	public void init(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return  (Double) agent.get("mass") / (Double) agent.get("density");
	}

}
