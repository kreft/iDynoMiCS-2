package agent.state.library;

import agent.Agent;
import agent.Body;
import agent.state.SecondaryState;
import agent.state.State;

public class CoccoidCenter extends SecondaryState implements State {

	/**
	 * input body
	 * @author baco
	 *
	 */
	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		// V = 4/3 Pi r^3
		return ((Body) agent.get(input[0])).getJoints().get(0);
	}
	
	public State copy()
	{
		return this;
	}
}
