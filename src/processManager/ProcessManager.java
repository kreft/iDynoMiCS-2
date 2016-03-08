package processManager;

import java.util.LinkedHashMap;

import javax.swing.AbstractAction;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.XmlLabel;
import generalInterfaces.XMLable;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import idynomics.NameRef;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelRequirement;

/**
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ProcessManager implements XMLable, AspectInterface, IsSubmodel
{
	/**
	 * The name of this {@code ProcessManager}, for reporting.
	 */
	protected String _name;
	/**
	 * In the event of a time-clash between {@code ProcessManager}s, the one
	 * with greater priority goes first.
	 */
	protected int _priority;
	/**
	 * The time at which this should next perform its step.
	 */
	protected double _timeForNextStep = 0.0;
	/**
	 * How often this should perform its step.
	 */
	protected double _timeStepSize;
	/**
	 * Set to true for extra system output.
	 */
	protected boolean _debugMode = false;
	/**
     * The aspect registry... TODO
     */
    public AspectReg<Object> aspectRegistry = new AspectReg<Object>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ProcessManager()
	{
		
	}
	
	public void init(Element xmlElem)
	{
		//FIXME quick fix: cut/paste from
		//"public static ProcessManager getNewInstance(Node xmlNode)"
		
		this.loadAspects(xmlElem);
		/*
		 * Read in the process attributes. 
		 */
		Element p = (Element) xmlElem;
		this.setName( p.getAttribute( XmlLabel.nameAttribute ));
		/* Process priority - default is zero. */
		int priority = 0;
		if ( p.hasAttribute(NameRef.processPriority) )
			priority = Integer.valueOf(p.getAttribute(NameRef.processPriority));
		this.setPriority(priority);
		/* Initial time to step. */
		double time = Idynomics.simulator.timer.getCurrentTime();
		if ( p.hasAttribute(NameRef.initialStep) )
			time = Double.valueOf( p.getAttribute(NameRef.initialStep) );
		this.setTimeForNextStep(time);
		/* Time step size. */
		time = Idynomics.simulator.timer.getTimeStepSize();
		if ( p.hasAttribute("timerStepSize") )
			time = Double.valueOf( p.getAttribute("timerStepSize") );
		this.setTimeStepSize(time);
	}
	
	/**
	 * Implements XMLable interface, return new instance from xml Node.
	 * 
	 * @param xmlNode
	 * @return
	 */
	public static ProcessManager getNewInstance(Node xmlNode)
	{
		ProcessManager proc = (ProcessManager) XMLable.getNewInstance(xmlNode);
		proc.init((Element) xmlNode);
		return proc;
	}
	
	public static ProcessManager getNewInstance(String className)
	{
		return (ProcessManager) XMLable.getNewInstance(className);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Return the aspect registry (implementation of aspect interface).
	 */
	public AspectReg<?> reg()
	{
		return aspectRegistry;
	}
	
	/**
	 * \brief Get this {@code ProcessManager}'s name.
	 * 
	 * @return {@code String} name.
	 */
	public String getName()
	{
		return this._name;
	}
	
	/**
	 * \brief Set this {@code ProcessManager}'s name.
	 * 
	 * @param {@code String} name.
	 */
	public void setName(String name)
	{
		this._name = name;
	}
	
	/**
	 * \brief Set the priority of this {@code ProcessManager}.
	 * 
	 * <p>A higher priority {@code ProcessManager} goes first if two have a
	 * time clash in the {@code Compartment} they belong to.</p>
	 * 
	 * @param priority Any integer value.
	 */
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
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step(EnvironmentContainer environment, AgentContainer agents)
	{
		/*
		 * This is where subclasses of Mechanism do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(environment, agents);
		/*
		 * Move the time for next step forward by the step size.
		 */
		this._timeForNextStep += this._timeStepSize;
		/*
		 * Report if in debugging mode.
		 */
		if ( this._debugMode )
		{
			System.out.println(this.getName() + 
								" timeForNextStep = "+this._timeForNextStep);
		}
	}
	
	protected abstract void internalStep(
					EnvironmentContainer environment, AgentContainer agents);
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer report()
	{
		StringBuffer out = new StringBuffer();
		
		return out;
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	
	public LinkedHashMap<String, Class<?>> getAttributes()
	{
		// TODO
		return new LinkedHashMap<String, Class<?>>();
	}
	
	public LinkedHashMap<AbstractAction,SubmodelRequirement>
														getAllSubmodelMakers()
	{
		// TODO
		return new LinkedHashMap<AbstractAction,SubmodelRequirement>();
	}
	
	public IsSubmodel getLastMadeSubmodel()
	{
		// TODO
		return null;
	}
}