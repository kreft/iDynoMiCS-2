package agent.state;

import generalInterfaces.AspectInterface;
import generalInterfaces.Copyable;
import generalInterfaces.XMLable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import agent.AspectReg;
import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import dataIO.XmlHandler;

/**
 * Secondary states contain a description of how secondary states can be
 * calculated from primary states. Secondary states return the value of this
 * calculated states when queried and are intended to prevent errors due to
 * state values that have not been updated, and they reduce memory capacity 
 * since the can be set on species level rather than agent level.
 * @author baco
 *
 */
public abstract class Calculated implements Copyable, XMLable {
	
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
	 * General constructor from xmlNodes, returns a new instance directly from
	 * an xml node. Overwrite this method in implementing class if the class
	 * needs constructor arguments (they should be stored within the Node).
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		Calculated obj = (Calculated) XMLable.getNewInstance(xmlNode);
		obj.setInput(XmlHandler.gatherAttribute(xmlNode, "input"));
		return obj;
	}
		
	/**
	 * return the current (up-to-date) value of the secondary state.
	 */
	public abstract Object get(AspectInterface registry);
}