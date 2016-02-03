package agent;

import java.util.HashMap;

import agent.state.PrimaryState;
import agent.state.State;

/**
 * Manages a hasmap with all aspects of an agent or species
 * @author baco
 *
 */
public abstract class AspectRegistry {
	
	/**
	 * The states HashMap stores all primary, secondary states and events.
	 */
	protected HashMap<String, State> _states = new HashMap<String, State>();
    
	/**
	 * returns the a state, not the object stored in the state!
	 * @param state
	 * @return
	 */
    public abstract State getState(String state);
    
    /**
     * returns true if a state is defined for the agent or species
     * @param state
     * @return
     */
    public abstract boolean isGlobalState(String state);
	
    /**
     * returns true if a state is defined in the local AspectRegistry
     * @param name
     * @return
     */
	public boolean isLocalState(String name)
	{
		return _states.containsKey(name) ? true : false;
	}
	
	/**
	 * \brief setter method for any state NOTE: do not use to set objects there
	 * are not an instance of State!
	 * @param name
	 * 			name of the state (String)
	 * @param state
	 * 			Object that contains the value of the state.
	 */
	public void setState(String name, State state)
	{
		_states.put(name, state);
	}
	
	/**
	 * Create a new primary state and set an object
	 * @param name
	 * @param state
	 */
	public void setPrimary(String name, Object object)
	{
		State aState = new PrimaryState();
		aState.set(object);
		_states.put(name, aState);
	}

	/**
	 * set an object or state, set should be able to handle any type of state 
	 * you throw at it.
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