package agent;
import org.w3c.dom.Node;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import dataIO.XmlLoad;
import agent.event.Event;
import agent.state.*;
import generalInterfaces.AspectInterface;
import generalInterfaces.Quizable;
import idynomics.Compartment;
import idynomics.NameRef;

/**
 * 
 * @author baco
 *
 */
public class Agent implements Quizable, AspectInterface
{

	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    final int uid = ++UNIQUE_ID;
    
    /**
     * The compartment the agent is currently in
     */
    protected Compartment compartment;
    
    public AspectReg<Object> aspectRegistry = new AspectReg<Object>();
    
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{

	}
	
	public Agent(Node xmlNode)
	{
		XmlLoad.loadStates(this, xmlNode);
		this.init();
	}
	
	/**
	 * NOTE: this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		agent.aspectRegistry.duplicate(this);
		this.init();
		this.compartment = agent.getCompartment();
	}
	
	/**
	 * 
	 */
	public void init()
	{
		aspectRegistry.addSubModule((Species) SpeciesLib.get(aspectRegistry.isGlobalAspect(NameRef.species) ? 
				(String) get(NameRef.species) : ""));
	}


	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	public AspectReg<?> registry() {
		return aspectRegistry;
	}
	
	/*
	 * returns object stored in Agent state with name "name". If the state is
	 * not found it will look for the Species state with "name". If this state
	 * is also not found this method will return null.
	 */
	public Object get(String key)
	{
		return aspectRegistry.getValue(this, key);
	}
	
	public void set(String key, Object aspect)
	{
		aspectRegistry.set(key, aspect);
	}

	/**
	 * return the compartment the agent is registered to
	 * @return
	 */
	public Compartment getCompartment()
	{
		return compartment;
	}
	
	/**
	 * Set the compartment of this agent.
	 * NOTE: this method should only be called from the compartment when it
	 * is registering a new agent.
	 * @param compartment
	 */
	public void setCompartment(Compartment compartment)
	{
		this.compartment = compartment;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * Perform an event.
	 * @param event
	 */
	public void event(String event)
	{
		event(event, null, null);
	}
	
	public void event(String event, Double timestep)
	{
		event(event, null, timestep);
	}
	
	public void event(String event, Agent compliant)
	{
		event(event, compliant, null);
	}
	
	public void event(String event, Agent compliant, Double timestep)
	{
		aspectRegistry.doEvent(this,compliant,timestep,event);
	}
	
	/*************************************************************************
	 * general methods
	 ************************************************************************/

	/**
	 * \brief: Registers the birth of a new agent with the agentContainer.
	 * note that the compartment field of the agent is set by the compartment
	 * itself.
	 */
	public void registerBirth() {
		compartment.addAgent(this);
	}

	/**
	 * return the unique identifier of the agent.
	 * @return
	 */
	public int identity() {
		return uid;
	}

	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}
