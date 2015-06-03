package mechanism;

import spatialgrid.SoluteGrid;
import idynomics.AgentContainer;

public abstract class Mechanism
{
	protected String _name;
	
	protected int _priority;
	
	protected Double _timeForNextStep;
	
	protected Double _timeStepSize;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Mechanism()
	{
		
	}
	
	public void init()
	{
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public String getName()
	{
		return this._name;
	}
	
	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	public int getPriority()
	{
		return this._priority;
	}
	
	public void setTimeForNextStep(Double newTime)
	{
		this._timeForNextStep = newTime;
	}
	
	public Double getTimeForNextStep()
	{
		return this._timeForNextStep;
	}
	
	public void setTimeStepSize(Double newStepSize)
	{
		this._timeStepSize = newStepSize;
	}
	
	public Double getTimeStepSize()
	{
		return this._timeStepSize;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public abstract void step(SoluteGrid[] solutes, AgentContainer agents);
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer report()
	{
		StringBuffer out = new StringBuffer();
		
		return out;
	}
	
}