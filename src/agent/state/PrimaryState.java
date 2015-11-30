package agent.state;

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
		switch (state.getClass().getSimpleName()) 
		{
			case "Double" : 
				copy.set(new Double((double) state));
				return copy;
			case "Integer" : 
				copy.set(new Integer((int) state));
				return copy;
			case "Boolean" : 
				copy.set(new Boolean((boolean) state));
				return copy;
			case "String" :  
				// copy.set(String.copyValueOf(((String) state).toCharArray()));
				// FIXME double check whether this works, Strings are immutable.
				copy.set(state); 
				return copy;
		}
		if (state instanceof Copyable)
		{
			copy.set(((Copyable) state).copy());
			return copy;
		} else {
			throw new Error("Unable to copy agent state");
		}
	}
	
}
