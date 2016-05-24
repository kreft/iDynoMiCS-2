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

	public void setField(String field, String value)
	{
		try {
		if (this.getClass().getField(field).getClass().equals(String.class))
			this.getClass().getField(field).set(this, value);
		} catch (IllegalArgumentException | IllegalAccessException | 
				NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

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
	
	@Override
	public String getXml() {
		// TODO Auto-generated method stub
		return null;
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
	

	public static Object getNewInstance(String input) {
		Event obj = (Event) XMLable.getNewInstance(input);
		obj.init(input);
		return obj;
	}


	public void init(Element xmlElem)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, "input");
		if (input != "")
			this.setInput(input);
	}
	
	
	private void init(String input) 
	{
		this.setInput(input);
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
