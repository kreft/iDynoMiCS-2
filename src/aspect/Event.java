package aspect;

import generalInterfaces.Copyable;
import generalInterfaces.Instantiatable;
import generalInterfaces.Redirectable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import dataIO.XmlRef;

/**
 * An Event is a special agent aspect that performs an "action". This action can
 * include mutations of agent states, interactions with the environment or with
 * other agents. An event does not store any information other than the agent
 * states it interacts with.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Event implements Copyable, Instantiatable, Redirectable
{
	/**
	 * General constructor from xmlNodes, returns a new instance directly from
	 * an xml node. Overwrite this method in implementing class if the class
	 * needs constructor arguments (they should be stored within the Node).
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		Event obj = (Event) Instantiatable.getNewInstance(xmlNode);
		obj.init((Element) xmlNode);
		return obj;
	}
	

	public static Object getNewInstance(String input) {
		Event obj = (Event) Instantiatable.getNewInstance(input);
		return obj;
	}


	public void init(Element xmlElem)
	{
		String fields = XmlHandler.gatherAttribute(xmlElem, XmlRef.fields);
		if (fields != "")
			this.redirect(fields);
	}
	
	
	/**
	 * Perform the event, this may include checking whether the event is
	 * applicable
	 * @param initiator
	 * @param compliant
	 * @param timeStep
	 */
	public abstract void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep);

	/**
	 * Events are general behavior patterns, copy returns this
	 */
	public Object copy()
	{
		return this;
	}

}
