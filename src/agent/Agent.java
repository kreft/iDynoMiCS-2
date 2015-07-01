package agent;

import java.util.ArrayList;
import java.util.LinkedList;

import agent.activity.*;
import agent.body.*;
import agent.state.*;
import spatialgrid.SoluteGrid;
import utility.Vector;

public class Agent
{
	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;

    /*************************************************************************
	 * General properties/variables
	 ************************************************************************/
    
	/**
	 * Activities represent the processes an agent can perform. For example
	 * grow, divide, conjugate, die, etc.
	 */
	protected LinkedList<Activity> activities = null;
    
	protected LinkedList<State> states = null;
	
    /**
     * index of this species in the species library
     */
	protected Integer speciesIndex = null;

	/**
	 * A list of all agent that are contained within this agent (plasmid, phage)
	 */
	protected LinkedList<Agent> internalAgents = null;
	
	/**
	 * Time at which this agent was created.
	 */
	protected Double _birthday 		= null;
	
	/**
	 * denotes the status of the cell: Dead, dividing, etc.
	 */
    protected Integer _agentStatus 	= null;
    
    /*************************************************************************
	 * Body and location variables
	 ************************************************************************/
    
    /**
     * The agentBody represents the morphology, size and location of the agent
     * physical interactions work on the Points or vertices of the body
     */
    Body agentBody 					= null;
    
    /**
     * The mass of all agent biomass components (lipids, DNA, capsule)
     */
    protected Double[] _mass 		= null;
    
    /**
     * The density of all agent biomass components (lipids, DNA, capsule)
     */
    protected Double[] _density 	= null;
    
    /*************************************************************************
	 * Episome variables
	 ************************************************************************/
    
    /**
     * copy number of plasmids or phages
     */
    private Integer _copyNumber 	= null;
    
    protected boolean _isRepressed 	= false;
	
    private Double _lastExchange 	= null;
	
    private Double _lastReception 	= null;
    
    /*************************************************************************
	 * Active (reaction) variables
	 ************************************************************************/

	/**
	 * Array of all the reactions this agent is involved in
	 */
	//public Reaction[] allReactions;
	
	/**
	 * Array of the reactions that are active
	 */
	protected ArrayList<Integer> reactionActive;
    
	/**
	 * Growth rate of this agent due to reactions
	 */
	protected Double[] growthRate;
    
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{
		
	}
	
	public void init()
	{
		int reactionIndex = 0;
				
		activities.add(new Conjugation(internalAgents.get(1)));
		activities.add(new SwitchReaction(reactionIndex));
	}
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public void addActivity(Activity newActivity)
	{
		activities.add(newActivity);
	}

	public boolean isRepressed() {
		return _isRepressed;
	}

	public Double getLastReception() {
		return _lastReception;
	}

	public void setLastReception(Double lastReception) {
		this._lastReception = lastReception;
	}

	public Double getLastExchange() {
		return _lastExchange;
	}

	public void setLastExchange(Double lastExchange) {
		this._lastExchange = lastExchange;
	}
	
	public Integer getCopyNumber() {
		return _copyNumber;
	}

	public void setCopyNumber(Integer copyNumber) {
		this._copyNumber = copyNumber;
	}

	/**
	 * 
	 * @return the total mass of the agent (including all components)
	 */
	public Double getMass()
	{
		return Vector.sum(_mass);
		
	}


	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step(Double timeStepSize, SoluteGrid[] solutes)
	{
		
	}



	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}
