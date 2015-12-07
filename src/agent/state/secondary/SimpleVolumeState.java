package agent.state.secondary;

import agent.Agent;
import agent.state.SecondaryState;

public class SimpleVolumeState extends SecondaryState {
	
	////////////////////////////////////////////////
	// input index
	// 0: mass
	// 1: density
	////////////////////////////////////////////////

	public SimpleVolumeState() { }
	
	public SimpleVolumeState(String state) { 
		this.set(state);
	}

	public Object get(Agent agent)
	{
		return  (double) agent.get(input[0]) / (double) agent.get(input[1]);
	}

}
