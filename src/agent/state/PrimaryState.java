package agent.state;

import utility.Copier;
import generalInterfaces.Copyable;
import agent.Agent;

public class PrimaryState implements State {
	protected Object state;

	public void set(Object state)
	{
		this.state = state;
	}
	
	public Object get(Agent agent)
	{
		return state;
	}

	public State copy()
	{
		// TODO: we must have deep copies, check whether there is any better
		// way of doing it
		// TODO: more objects from primitives to be included
		State copy = new PrimaryState();
		copy.set(Copier.copy(state));
		return copy;
	}
	
}
