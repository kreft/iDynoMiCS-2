package agent;

import java.util.HashMap;

import org.w3c.dom.Node;

import dataIO.XmlLoad;
import agent.event.Event;
import agent.state.*;
import idynomics.Compartment;

public class Agent implements StateObject
{

	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    final int uid = ++UNIQUE_ID;

	/**
	 * The states HashMap stores all primary and secondary states.
	 * FIXME Bas: now also includes all events an agent can perform.. consider
	 * renaming
	 */
	protected HashMap<String, State> _states = new HashMap<String, State>();
    
    /**
     * Used to fetch species states.
     * creates a new empty species for if no species is defined. (we may want to
     * do this slightly different).
     */
    Clade clade = new Clade();
    
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
		clade = CladeLib.get((String) get("clade"));
	}
	
	/**
	 * TODO this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		for (String key : agent._states.keySet())
			this._states.put(key, agent.getState(key).copy());
		clade = CladeLib.get((String) get("clade"));
		this.compartment = agent.getCompartment();
	}
	
	public void init()
	{
				
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
		if (isLocalState(name))
			return _states.get(name);
		else
		{
			System.out.println("Warning: agent state " + name + " not defined.");
			return null;
		}
	}
	
	public boolean isLocalState(String name)
	{
		if (_states.containsKey(name))
			return true;
		else
			return false;
	}
	
	public boolean isGlobalState(String name)
	{
		if (isLocalState(name))
			return true;
		else
			return clade.isGlobalState(name);
	}
	
	/*
	 * returns object stored in Agent state with name "name". If the state is
	 * not found it will look for the Species state with "name". If this state
	 * is also not found this method will return null.
	 */
	public Object get(String name)
	{
		if (this.isLocalState(name))
			return getState(name).get(this);
		else if (clade.isLocalState(name))
			return clade.getState(name).get(this);
		else
			return null;
	}
	
	/**
	 * \brief general setter method for any Agent state
	 * @param name
	 * 			name of the state (String)
	 * @param state
	 * 			Object that contains the value of the state.
	 */
	public void setState(String name, State state)
	{
		_states.put(name, state);
	}
	
	public void setPrimary(String name, Object state)
	{
		State aState = new PrimaryState();
		aState.set(state);
		_states.put(name, aState);
	}

	/**
	 * set should be able to handle any type of state you throw at it.
	 * @param name
	 * @param state
	 */
	public void set(String name, Object state)
	{
		if (state instanceof State)
			setState(name,(State) state);
		else
			setPrimary(name, state);
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
