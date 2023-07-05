package aspect;

import org.w3c.dom.Element;

import generalInterfaces.Copyable;
import generalInterfaces.Redirectable;
import instantiable.Instantiable;
import settable.Settable;


/**
 * An Event is a special agent aspect that performs an "action". This action can
 * include mutations of agent states, interactions with the environment or with
 * other agents. An event does not store any information other than the agent
 * states it interacts with.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Event implements Copyable, Instantiable, Redirectable
{

	public void instantiate(Element xmlElem, Settable parent)
	{
		this.redirect(xmlElem);		
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
