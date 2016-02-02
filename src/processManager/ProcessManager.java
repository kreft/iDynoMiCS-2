package processManager;

import java.util.Collection;

import boundary.Boundary;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

public abstract class ProcessManager
{
	protected String _name;
	
	protected int _priority;
	
	protected double _timeForNextStep = 0.0;
	
	protected double _timeStepSize;
	
	protected boolean _debugMode = false;
	
	
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
	
	public void setName(String name)
	{
		this._name = name;
	}
	
	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	public int getPriority()
	{
		return this._priority;
	}
	
	public void setTimeForNextStep(double newTime)
	{
		this._timeForNextStep = newTime;
	}
	
	public double getTimeForNextStep()
	{
		return this._timeForNextStep;
	}
	
	public void setTimeStepSize(double newStepSize)
	{
		this._timeStepSize = newStepSize;
	}
	
	public double getTimeStepSize()
	{
		return this._timeStepSize;
	}
	
	public void debugMode()
	{
		this._debugMode = true;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param boundaries
	 */
	public void showBoundaries(Collection<Boundary> boundaries)
	{
		
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step(EnvironmentContainer environment, AgentContainer agents)
	{
		//System.out.println("STEP");//bughunt
		//System.out.println("timeForNextStep = "+_timeForNextStep);//bughunt
		//System.out.println("timeStepSize = "+_timeStepSize);//bughunt
		/*
		 * This is where subclasses of Mechanism do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(environment, agents);
		/*
		 * Increase the 
		 */
		this._timeForNextStep += this._timeStepSize;
		
		if (_debugMode)
			System.out.println(getName() + " timeForNextStep = "+_timeForNextStep); 
	}
	
	protected abstract void internalStep(EnvironmentContainer environment,
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