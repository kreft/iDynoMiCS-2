package agent.state;

import utility.Copier;
import generalInterfaces.Copyable;
import generalInterfaces.Duplicable;
import agent.Agent;

/**
 * PrimaryState objects can store any type of object. PrimaryStates are intended
 * to store all primary characteristic parameters and variables of an agent or 
 * species.
 * @author baco
 *
 */
public class PrimaryState implements State {
	protected Object state;

	/**
	 * Store an object as primary state
	 */
	public void set(Object state)
	{
		this.state = state;
	}
	
	/**
	 * Retrieve the object that is stored as primary state
	 */
	public Object get(Agent agent)
	{
		return state;
	}

	/**
	 * return a duplicate of the primary state, yet set a new agent as owner
	 * if applicable
	 * NOTE: this method may be replaced when we switch to an other solution for
	 * states that require ownership definition.
	 */
	public State duplicate(Agent agent)
	{
		State copy = new PrimaryState();
		if (state instanceof Duplicable)
			copy.set(((Duplicable) state).copy(agent));
		else
			copy.set(Copier.copy(state));
		return copy;
	}
	
}
