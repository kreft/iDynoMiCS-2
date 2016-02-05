package agent.state;

import agent.AspectReg;

public abstract class Calculated {

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
	public Calculated copy()
	{
		return this;
	}
	
	/**
	 * return the current (up-to-date) value of the secondary state.
	 */
	public abstract Object get(AspectReg registry);
}
