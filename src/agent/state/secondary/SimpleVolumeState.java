package agent.state.secondary;

import agent.Agent;
import agent.state.SecondaryState;
import agent.state.State;


public class SimpleVolumeState extends SecondaryState implements State {
	
	/**
	 * input mass, density
	 * @author baco
	 *
	 */
	public SimpleVolumeState(String input)
	{
		this.setInput(input);
	}
	
	public SimpleVolumeState()
	{
		
	}

	// input mass, density
	
	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return  (double) agent.get(input[0]) / (double) agent.get(input[1]);
	}
	
	public State copy()
	{
		return this;
	}

}
