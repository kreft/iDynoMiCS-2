package agent.state.library;

import agent.Agent;
import agent.Body;
import agent.state.SecondaryState;
import agent.state.State;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;

public class SimpleSurface extends SecondaryState implements State {
	
	/**
	 * 
	 * @param input: body
	 */
	public void set(Object state)
	{

	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(input[0])).getSurface();
	}
	
	public State copy()
	{
		return this;
	}
}