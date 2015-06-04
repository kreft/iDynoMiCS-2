package agent;

import java.util.LinkedList;

import activities.Activity;
import activities.Conjugation;
import activities.SwitchReaction;
import agent.body.Body;
import spatialgrid.SoluteGrid;

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
     * 
     */
	protected Integer speciesIndex = null;

	/**
	 * A list of all agent that are contained within this agent (plasmid, phage)
	 */
	protected LinkedList<Agent> internalAgents = null;
	
	/**
	 * 
	 */
	protected LinkedList<Activity> activities = null;
	
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
