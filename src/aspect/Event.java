package aspect;

import generalInterfaces.Copyable;
import generalInterfaces.XMLable;

import org.w3c.dom.Node;

import dataIO.XmlHandler;

/**
 * An Event is a special agent aspect that performs an "action". This action can
 * include mutations of agent states, interactions with the environment or with
 * other agents. An event does not store any information other than the agent
 * states it interacts with.
 * @author baco
 *
 */
public abstract class Event implements Copyable, XMLable {
	
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
		return this.input;
	}
	
	/**
	 * General constructor from xmlNodes, returns a new instance directly from
	 * an xml node. Overwrite this method in implementing class if the class
	 * needs constructor arguments (they should be stored within the Node).
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		Event obj = (Event) XMLable.getNewInstance(xmlNode);
		obj.setInput(XmlHandler.gatherAttribute(xmlNode, "input"));
		return obj;
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
	 * Events are general behavior patterns, copy returns this
	 */
	public Object copy() {
		return this;
	}
}
