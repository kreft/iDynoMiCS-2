package agent;
import org.w3c.dom.Node;

import dataIO.XmlLoad;
import agent.event.Event;
import agent.state.*;
import generalInterfaces.Quizable;
import idynomics.Compartment;

public class Agent extends AspectRegistry implements Quizable
{

	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    final int uid = ++UNIQUE_ID;

    /**
     * Used to fetch species states.
     */
    protected Species species;
    
    /**
     * The compartment the agent is currently in
     */
    protected Compartment compartment;
    
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
	 * TODO this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		for (String key : agent._states.keySet())
			this._states.put(key, agent.getState(key).duplicate(this));
		this.init();
		this.compartment = agent.getCompartment();
	}
	
	public void init()
	{
		species = SpeciesLib.get(isLocalState("species") ? (String) get("species") : "");
	}


	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief general getter method for any primary Agent state
	 * @param name
	 * 			name of the state (String)
	 * @return Object of the type specific to the state
	 */
	public State getState(String name)
	{
		//return (isLocalState(name) ?  _states.get(name) : null);
		if (isLocalState(name))
			return _states.get(name);
		else if (isGlobalState(name))
			return species.getState(name);	 
		{
			//TODO muted for testing purposes
			//System.out.println("Warning: agent state " + name + " not defined.");
			return null;
		}
	}
	
	public boolean isGlobalState(String name)
	{
		return isLocalState(name) ? true : species.isGlobalState(name);
	}
	
	/*
	 * returns object stored in Agent state with name "name". If the state is
	 * not found it will look for the Species state with "name". If this state
	 * is also not found this method will return null.
	 */
	public Object get(String name)
	{
		State state = getState(name);
		if (state == null)
			return null;
		else
			return getState(name).get(this);
	}

	public Compartment getCompartment()
	{
		return compartment;
	}
	
	public void setCompartment(Compartment compartment)
	{
		this.compartment = compartment;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
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
		Object myEvent = this.get(event);
		if (myEvent != null)
			((Event) myEvent).start(this, compliant, timestep);
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

	public int identity() {
		return uid;
	}

	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}
