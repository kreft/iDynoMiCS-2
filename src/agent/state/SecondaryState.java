package agent.state;

import generalInterfaces.AspectInterface;
import generalInterfaces.Copyable;
import agent.Agent;
import agent.AspectReg;

/**
 * Secondary states contain a description of how secondary states can be
 * calculated from primary states. Secondary states return the value of this
 * calculated states when queried and are intended to prevent errors due to
 * state values that have not been updated, and they reduce memory capacity 
 * since the can be set on species level rather than agent level.
 * @author baco
 *
 */
public abstract class SecondaryState implements State, Copyable {
	
	/**
	 * input states
	 */
	protected String[] input;

	/**
	 * method that sets the input from a comma separated String.
	 * @param input
	 */
	public void setInput(String input)
	{
		this.input = input.split(",");
	}
	
	/**
	 * returns the input String array of this state
	 * @return
	 */
	public String[] getInput()
	{
		return input;
	}
	
	/**
	 * return a copy of this state.
	 */
	public State copy()
	{
		return this;
	}
	
	/**
	 * return a duplicate of this state (duplicable interface)
	 * NOTE: this method may be removed when we switch to an other solution for
	 * states that require ownership definition.
	 */
	public State duplicate(Agent agent)
	{
		return this;
	}
	
	/**
	 * return the current (up-to-date) value of the secondary state.
	 */
	public abstract Object get(AspectInterface registry);
}