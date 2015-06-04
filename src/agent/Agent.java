package agent;

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
    
    /**
     * The agentBody represents the location
     */
    Body agentBody = null;
    
    /**
     * The mass of the agent
     */
    protected Double _mass = null;
    
    /**
     * density of the cell
     */
    protected Double _density = null;
    
    /**
     * The mass of the EPS capsule
     */
    protected Double _internalEPS = null;
    
    /**
     * density of the EPS capsule
     */
    protected Double _EPSDensity = null;
    
    /**
     * 
     */
	protected Integer speciesIndex = null;
    
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{
		
	}
	
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	

	
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
