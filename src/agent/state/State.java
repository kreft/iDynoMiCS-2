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
	
	public void set(Object state);
	
	public Object get(Agent agent);

	public State copy();

	public State duplicate(Agent agent);
}
