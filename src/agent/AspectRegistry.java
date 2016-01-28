package agent;

import java.util.HashMap;

import agent.state.PrimaryState;
import agent.state.State;

public abstract class AspectRegistry {
	
	/**
	 * The states HashMap stores all primary, secondary states and events.
	 */
	protected HashMap<String, State> _states = new HashMap<String, State>();
    
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
	private void setState(String name, State state)
	{
		_states.put(name, state);
	}
	
	private void setPrimary(String name, Object state)
	{
		State aState = new PrimaryState();
		aState.set(state);
		_states.put(name, aState);
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