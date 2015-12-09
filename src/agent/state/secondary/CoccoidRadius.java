package agent.state.secondary;

import agent.Agent;
import agent.state.State;
import utility.ExtraMath;

public class CoccoidRadius  implements State {

	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		// V = 4/3 Pi r^3
		return ExtraMath.radiusOfASphere((double) agent.get("volume"));
	}
	
	public State copy()
	{
		return this;
	}
}

