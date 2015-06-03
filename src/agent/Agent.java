package agent;

import spatialgrid.SoluteGrid;

public class Agent
{
	/**
	 * The location of this agent in space. Since not all agents will have
	 * location (or may only have location in certain environments), this is
	 * initialised as a null.
	 */
	protected Double[] _location = null;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{
		
	}
	
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public void setLocation(Double[] location)
	{
		this._location = location;
	}
	
	public Double[] getLocation()
	{
		return this._location;
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
