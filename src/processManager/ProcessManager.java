package processManager;

import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;
import boundary.Boundary;
import generalInterfaces.XMLable;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import idynomics.Timer;
import utility.Helper;

public abstract class ProcessManager implements XMLable, AspectInterface
{
	protected String _name;
	
	protected int _priority;
	
	protected double _timeForNextStep = 0.0;
	
	protected double _timeStepSize;
	
	protected boolean _debugMode = false;
	
	/**
     * The aspect registry
     */
    public AspectReg<Object> aspectRegistry = new AspectReg<Object>();
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ProcessManager()
	{
		
	}
	
	public void init()
	{
		
	}

	/**
	 * implements XMLable interface, return new instance from xml Node
	 * @param xmlNode
	 * @return
	 */
	public static ProcessManager getNewInstance(Node xmlNode)
	{
		Element p = (Element) xmlNode;
		ProcessManager proc = (ProcessManager) XMLable.getNewInstance(xmlNode);
		AspectInterface.loadAspects(proc, xmlNode);
		
		proc.setName( p.getAttribute( NameRef.xmlName ));
		proc.setPriority( Integer.valueOf( 
				p.getAttribute( NameRef.processPriority) ));
		proc.setTimeForNextStep( Double.valueOf(
				p.getAttribute( NameRef.initialStep )));
		proc.setTimeStepSize( Helper.setIfNone(proc.getDouble("timerStepSize"), 
				Timer.getTimeStepSize())); // TODO: Bas [16,02,16] this is a work around, nicefy

		
		proc.init();
		return proc;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * return the aspect registry (implementation of aspect interface).
	 */
	public AspectReg<?> reg() {
		return aspectRegistry;
	}
	
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