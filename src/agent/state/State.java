package agent.state;

import agent.Agent;

/*
 * A primary (or elementary state) is directly stored as data carrying object
 * secondary states are not directly stored but describe how the state values
 * can be calculated from primary states. Thus when a primary state changes
 * all secondary states that are (partially) defined by that primary state
 * change to.
 * 
 * Primary states are stored as a PrimaryState object, secondary states are
 * either stored as a CalculatedState object (state from an anonymous class) or
 * as a dedicated (predefined) secondary state object, see agent.state.secondary
 */
public interface State {
	
	/**
	 * Set the state
	 * @param state
	 */
	public void set(Object state);
	
	/**
	 * return the value of the state (either direct or calulated)
	 * @param agent
	 * @return
	 */
//	public Object get(Agent agent);

	/**
	 * return a duplicate of this state (duplicable interface)
	 * NOTE: this method may be removed when we switch to an other solution for
	 * states that require ownership definition.
	 */
	public State duplicate(Agent agent);
}
