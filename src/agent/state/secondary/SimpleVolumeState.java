package agent.state.secondary;

import agent.Agent;
import agent.state.State;

public class SimpleVolumeState implements State {

	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return  (double) agent.get("mass") / (double) agent.get("density");
	}

}
