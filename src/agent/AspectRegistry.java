package agent;

import java.util.HashMap;

import agent.event.Event;
import agent.state.PrimaryState;
import agent.state.State;

public abstract class AspectRegistry {
	
	/**
	 * The states HashMap stores all primary and secondary states.
	 */
	protected HashMap<String, State> _states = new HashMap<String, State>();
	
    /**
	 * All activities owned by this Agent and whether they are currently enabled
	 * or disabled.
     */
    protected HashMap<String, Event> _events = new HashMap<String, Event>();
    
    public abstract State getState(String state);
    
    public abstract boolean isGlobalState(String state);
	
	public boolean isLocalState(String name)
	{
		return _states.containsKey(name) ? true : false;
	}
	
	/**
	 * \brief general setter method for any Agent state
	 * @param name
	 * 			name of the state (String)
	 * @param state
	 * 			Object that contains the value of the state.
	 */
	public void setState(String name, State state)
	{
		_states.put(name, state);
	}
	
	public void setPrimary(String name, Object state)
	{
		State aState = new PrimaryState();
		aState.set(state);
		_states.put(name, aState);
	}
	
	public void setEvent(String name, Event event)
	{
		_events.put(name, event);
	}

	/**
	 * set should be able to handle any type of state you throw at it.
	 * @param name
	 * @param state
	 */
	public void set(String name, Object state)
	{
		if (state instanceof State)
			setState(name,(State) state);
		else 
			setPrimary(name, state);
	}
}