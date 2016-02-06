package agent.event;

import generalInterfaces.AspectInterface;
import generalInterfaces.Copyable;
import agent.Agent;

/**
 * An Event is a special agent aspect that performs an "action". This action can
 * include mutations of agent states, interactions with the environment or with
 * other agents. An event does not store any information other than the agent
 * states it interacts with.
 * @author baco
 *
 */
public abstract class Event implements Copyable {
	
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
	 * returns the input String array of this event
	 * @return
	 */
	public String[] getInput()
	{
		return input;
	}
	
	/**
	 * Perform the event, this may include checking whether the event is
	 * applicable
	 * @param initiator
	 * @param compliant
	 * @param timeStep
	 */
	public abstract void start(AspectInterface initiator, AspectInterface compliant, Double timeStep);

	/**
	 * return a copy of this event.
	 */
	public abstract Object copy();
	
}
