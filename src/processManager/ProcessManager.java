package processManager;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.Log.Tier;
import dataIO.Log;
import dataIO.XmlHandler;
import generalInterfaces.Instantiatable;
import generalInterfaces.Redirectable;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;

/**
 * \brief Abstract class for managing a process within a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ProcessManager implements Instantiatable, AspectInterface,
		NodeConstructor, Redirectable
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
	 * The aspect registry... TODO
	 */
	private AspectReg _aspectRegistry = new AspectReg();
	/**
	 * Reference to the environment of the compartment this process belongs to.
	 * Contains a reference to the compartment shape.
	 */
	protected EnvironmentContainer _environment;
	/**
	 * Reference to the agents of the compartment this process belongs to.
	 * Contains a reference to the compartment shape.
	 */
	protected AgentContainer _agents;
	
	/**
	 * 
	 */
	protected String _compartmentName;
	
	private long _realTimeTaken = 0;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief Initialise the process from XML protocol file, plus the relevant
	 * information about the compartment it belongs to.
	 * 
	 * @param xmlElem Relevant part of the XML protocol file.
	 * @param environment The {@code EnvironmentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @param agents The {@code AgentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @param compartmentName The name of the {@code Compartment} this process
	 * belongs to.
	 */
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		this.init(environment, agents, compartmentName);
		
		if (xmlElem != null)
			this.loadAspects(xmlElem);
		/*
		 * Read in the process attributes. 
		 */
		Element p = (Element) xmlElem;
		this.setName( XmlHandler.obtainAttribute(p, XmlRef.nameAttribute, this.defaultXmlTag()));
		/* Process priority - default is zero. */
		int priority = 0;
		if ( XmlHandler.hasAttribute(p, XmlRef.processPriority) )
			priority = Integer.valueOf(p.getAttribute(XmlRef.processPriority));
		this.setPriority(priority);
		/* Initial time to step. */
		double time = Idynomics.simulator.timer.getCurrentTime();
		if ( XmlHandler.hasAttribute(p, XmlRef.processFirstStep) )
			time = Double.valueOf( p.getAttribute(XmlRef.processFirstStep) );
		this.setTimeForNextStep(time);
		/* Time step size. */
		time = Idynomics.simulator.timer.getTimeStepSize();
		if ( XmlHandler.hasAttribute(p, XmlRef.processTimeStepSize) )
			time = Double.valueOf( p.getAttribute(XmlRef.processTimeStepSize) );
		this.setTimeStepSize(time);
		
		String fields = XmlHandler.gatherAttribute(xmlElem, XmlRef.fields);
		if (fields != null)
			this.redirect(fields);
		
		Log.out(Tier.EXPRESSIVE, this._name + " loaded");
	}
	
	/**
	 * stripped down init required for JUnit tests without xml
	 * FIXME only used by unit tests consider removing
	 * @param environment
	 * @param agents
	 * @param compartmentName
	 */
	public void init(EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		this._environment = environment;
		this._agents = agents;
		this._compartmentName = compartmentName;
	}
	
	/**
	 * Generic init for Instantiatable implementation
	 * @param xmlElem
	 * @param compartment
	 */
	public void init(Element xmlElem, NodeConstructor parent)
	{
		this.init(xmlElem, ((Compartment) parent).environment, 
				((Compartment) parent).agents, ((Compartment) parent).getName());
	}
	
	/**
	 * get new instance
	 * TODO this is actually a bypasses  of the interface, consider renaming
	 * @param xmlNode Relevant part of the XML protocol file.
	 * @param environment The {@code EnvironmentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @param agents The {@code AgentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @return New process manager.
	 */
	public static ProcessManager getNewInstance(Node xmlNode, 
			EnvironmentContainer environment, AgentContainer agents, 
			String CompartmentName)
	{
		ProcessManager proc = (ProcessManager) Instantiatable.getNewInstance(xmlNode);
		proc.init((Element) xmlNode, environment, agents, CompartmentName);
		return proc;
	}

	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * ***********************************************************************/
	
	/**
	 * \brief Return the aspect registry (implementation of aspect interface).
	 */
	public AspectReg reg()
	{
		return _aspectRegistry;
	}
	
	/**
	 * \brief Get this {@code ProcessManager}'s name.
	 * 
	 * @return {@code String} name.
	 */
	public String getName()
	{
		if (this._name == null)
			return "";
		else
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
	
	/**
	 * @return The priority of this {@code ProcessManager}.
	 */
	public int getPriority()
	{
		return this._priority;
	}
	
	/**]
	 * \brief Set the time point at which this will next step: useful for
	 * testing.
	 * 
	 * @param newTime The time point at which this should next step.
	 */
	public void setTimeForNextStep(double newTime)
	{
		this._timeForNextStep = newTime;
	}
	
	/**
	 * @return The time point at which this will next step.
	 */
	public double getTimeForNextStep()
	{
		return this._timeForNextStep;
	}
	
	/**
	 * \brief Set the time step directly: useful for testing.
	 * 
	 * @param newStepSize Time step to use.
	 */
	public void setTimeStepSize(double newStepSize)
	{
		this._timeStepSize = newStepSize;
	}
	
	/**
	 * @return The local time step.
	 */
	public double getTimeStepSize()
	{
		return this._timeStepSize;
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	/**
	 * \brief Perform the step of this process manager, also updating its local
	 * time manager.
	 * 
	 * @param environment The {@code EnvironmentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @param agents The {@code AgentContainer} of the
	 * {@code Compartment} this process belongs to.
	 */
	public void step()
	{
		long tick = System.currentTimeMillis();
		/*
		 * This is where subclasses of ProcessManager do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep();
		/*
		 * Move the time for next step forward by the step size.
		 */
		this._timeForNextStep += this._timeStepSize;
		/*
		 * 
		 */
		long tock = System.currentTimeMillis();
		this._realTimeTaken += tock  - tick;
		Tier level = Tier.DEBUG;
		if ( Log.shouldWrite(level) )
		{
			Log.out(level,
					this._name+": timeForNextStep = "+this._timeForNextStep);
			Log.out(level,
					"    real time taken = "+((tock - tick)*0.001)+" seconds");
		}
		
	}
	
	/**
	 * \brief Perform the internal step for this process manager: this will be
	 * implemented by each sub-class of {@code ProcessManager}.
	 */
	protected abstract void internalStep();
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/
	
	public long getRealTimeTaken()
	{
		return this._realTimeTaken;
	}
	
	/* ***********************************************************************
	 * NODE CONSTRUCTION
	 * **********************************************************************/
	
	public static List<String> getAllOptions()
	{
		return Idynomics.xmlPackageLibrary.getAll("processManager.library.");
	}
	
	
	/**
	 * 
	 */
	public ModelNode getNode()
	{
		ModelNode modelNode = new ModelNode(defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this._name);
		
		modelNode.add(new ModelAttribute(XmlRef.nameAttribute, 
						this._name, null, true ));
		
		modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
				this.getClass().getSimpleName(), null, false ));
		
		modelNode.add(new ModelAttribute(XmlRef.processPriority, 
				String.valueOf(this._priority), null, true ));
		
		modelNode.add(new ModelAttribute(XmlRef.processTimeStepSize, 
				String.valueOf(this._timeStepSize), null, true ));
		
		modelNode.add(new ModelAttribute(XmlRef.processFirstStep, 
				String.valueOf(this._timeForNextStep), null, true ));
		
		/* TODO: add aspects */
		
		for ( String key : this.reg().getLocalAspectNames() )
			modelNode.add(reg().getAspectNode(key));
		
		modelNode.addChildConstructor(new Aspect(reg()), 
				ModelNode.Requirements.ZERO_TO_MANY);
		
		return modelNode;
	}
	
	
	/**
	 * 
	 */
	public void setNode(ModelNode node) 
	{
		
	}

	/**
	 * 
	 */
	public void addChildObject(NodeConstructor childObject) 
	{

	}
	
	/**
	 * Remove processManager from the compartment
	 * NOTE a bit of a work around but this prevents the pm form having to have
	 * access to the compartment directly
	 */
	public void removeNode(String specifier)
	{
		Idynomics.simulator.deleteFromCompartment(this._compartmentName, this);
	}

	/**
	 * 
	 */
	public String defaultXmlTag() 
	{
		return XmlRef.process;
	}
		
	/**
	 * 
	 */
	public String getXml() 
	{
		return this.getNode().getXML();
	}
}