package aspect;

import generalInterfaces.Copyable;
import generalInterfaces.XMLable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.XmlHandler;

/**
 * An Event is a special agent aspect that performs an "action". This action can
 * include mutations of agent states, interactions with the environment or with
 * other agents. An event does not store any information other than the agent
 * states it interacts with.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Event implements Copyable, XMLable
{
	/**
	 * Ordered list of the names of input states.
	 */
	protected String[] input;

	/**
	 * \brief Set the input from a comma separated String.
	 * 
	 * @param input {@code String} ordered list of input names, separated by
	 * commas.
	 */
	public void setInput(String input)
	{
		/* Strip all whitespace. */
		input.replaceAll("\\s+","");
		/* Read in the inputs. */
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
		obj.init((Element) xmlNode);
		return obj;
	}
	
	public void init(Element xmlElem)
	{
		this.setInput(XmlHandler.gatherAttribute(xmlElem, "input"));
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
	public Object copy()
	{
		return this;
	}
}
