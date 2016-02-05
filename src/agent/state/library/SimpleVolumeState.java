package agent.state.library;

import agent.Agent;
import agent.state.SecondaryState;
import agent.state.State;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;


public class SimpleVolumeState extends SecondaryState implements State {
	
	/**
	 * input mass, density
	 * @author baco
	 *
	 */
	public void set(Object state)
	{

	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return  (double) agent.get(input[0]) / (double) agent.get(input[1]);
	}
	
	public State copy()
	{
		return this;
	}

}
