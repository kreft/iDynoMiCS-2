package agent;
import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.Log;
import dataIO.XmlLabel;
import dataIO.Log.tier;
import generalInterfaces.Quizable;
import idynomics.Compartment;
import idynomics.Idynomics;

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
    
    /**
     * The aspect registry
     */
    public AspectReg<Object> aspectRegistry = new AspectReg<Object>();
    
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{

	}
	
	/**
	 * Agent xml constructor
	 * @param xmlNode
	 */
	public Agent(Node xmlNode)
	{
		this.loadAspects(xmlNode);
		this.init();
	}
	
	/**
	 * NOTE: this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		this.aspectRegistry.duplicate(agent);
		this.init();
		this.compartment = agent.getCompartment();
	}
	
	/**
	 * Assign the correct species from the species library
	 */
	public void init()
	{
		String species;
		if ( this.isAspect(XmlLabel.species) )
		{
			species = this.getString(XmlLabel.species);
			
			Log.out(tier.DEBUG, "Agent belongs to species \""+species+"\"");
		}
		else
		{
			species = "";
			Log.out(tier.DEBUG, "Agent belongs to void species");
		}
		aspectRegistry.addSubModule( (Species) 
							Idynomics.simulator.speciesLibrary.get(species));
	}


	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	/**
	 * Allows for direct access to the aspect registry
	 */
	public AspectReg<?> reg() {
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
		event(event, null, 0.0);
	}
	
	public void event(String event, Double timestep)
	{
		event(event, null, timestep);
	}
	
	public void event(String event, Agent compliant)
	{
		event(event, compliant, 0.0);
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
