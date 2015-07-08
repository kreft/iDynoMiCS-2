package processManager;

import spatialGrid.SoluteGrid;
import idynomics.AgentContainer;

public abstract class ProcessManager
{
	protected String _name;
	
	protected int _priority;
	
	protected Double _timeForNextStep;
	
	protected Double _timeStepSize;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ProcessManager()
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
	
	public void step(SoluteGrid[] solutes, AgentContainer agents)
	{
		/*
		 * This is where subclasses of Mechanism do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(solutes, agents);
		/*
		 * Increase the 
		 */
		this._timeForNextStep += this._timeStepSize;
		
	}
	
	protected abstract void internalStep(SoluteGrid[] solutes,
											AgentContainer agents);
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer report()
	{
		StringBuffer out = new StringBuffer();
		
		return out;
	}
	
}