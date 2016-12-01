package agent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.XmlHandler;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Compartment;
import idynomics.Idynomics;
import instantiatable.Instantiatable;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import surface.Point;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Agent implements AspectInterface, Settable, Instantiatable
{
	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
	final int _uid = ++UNIQUE_ID;

	/**
	 * The compartment the agent is currently in
	 */
	protected Compartment _compartment;
	
	/**
	 * The aspect registry
	 */
	protected AspectReg _aspectRegistry = new AspectReg();
	private Settable _parentNode;

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	public Agent()
	{

	}
	
	/**
	 * Used by GUI, dummy agent.
	 */
	public Agent(Compartment comp)
	{
		this._compartment = comp;
	}

	/**
	 * \brief Construct an {@code Agent} with unknown {@code Compartment}.
	 * 
	 * <p>This is useful for agent introduction by a {@code ProcessManager}.
	 * </p>
	 * 
	 * @param xmlNode
	 */
	public Agent(Node xmlNode)
	{
		this.loadAspects(xmlNode);
	}
	
	/**
	 * NOTE Bit of a hack allows initiation of multiple random agents
	 * Agent xml constructor allowing for multiple randomized initial agents
	 * @param xmlNode
	 */
	public Agent(Node xmlNode, Compartment comp)
	{
		this.init(xmlNode, comp);
	}
	
	public void init(Node xmlNode, Compartment comp)
	{
		this._compartment = comp;
		/* initiate all random agents */
		NodeList temp = XmlHandler.getAll(xmlNode, XmlRef.spawnNode);
		if(temp.getLength() > 0)
		{
			for(int i = 0; i < temp.getLength(); i++)
			{
				/* TODO this is a cheat, make a standard method for this */
				int n = Integer.valueOf(XmlHandler.obtainAttribute(
						temp.item(i), XmlRef.numberOfAgents, this.defaultXmlTag()));
				double[] domain = Vector.dblFromString(XmlHandler.
						obtainAttribute(temp.item(i), XmlRef.spawnDomain, this.defaultXmlTag()));
				for(int j = 0; j < n-1; j++)
				{
					Agent extra = new Agent(xmlNode, randBody(domain));
					extra._compartment = comp;
					extra.registerBirth();
				}
				this.loadAspects(xmlNode);
				this.set(AspectRef.agentBody, randBody(domain));
			}
		}
		else
		{
			loadAspects(xmlNode);
		}
		this.init();
	}
	
	/**
	 * Instantiatable implementation
	 */
	public void instantiate(Element xmlElement, Settable parent)
	{
		//FIXME change entire class to just using parent
		this._parentNode = parent;
		this._compartment = (Compartment) parent.getParent();
		this.loadAspects(xmlElement);
		this.init();
		this.registerBirth();
	}
	
	/**
	 * \brief Quick fix to get a coccoid body at a random location in the
	 * region of physical space specified by domain.
	 * 
	 * @param domain
	 * @return
	 */
	public Body randBody(double[] domain)
	{
		double[] v = Vector.randomZeroOne(domain);
		Vector.timesEquals(v, domain);
		return new Body(new Point(v), 0.0);
	}

	public Agent(Node xmlNode, Body body)
	{
		this.loadAspects(xmlNode);
		this.set(AspectRef.agentBody, body);
		this.init();
	}

	public Agent(String species, Compartment comp)
	{
		this.set(XmlRef.species,species);
		this._compartment = comp;
		this.init();
	}

	public Agent(String species, Body body, Compartment comp)
	{
		this.set(XmlRef.species, species);
		this.set(AspectRef.agentBody, body);
		this._compartment = comp;
		this.init();
	}

	/**
	 * NOTE: this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		this._aspectRegistry.duplicate(agent);
		this.init();
		this._compartment = agent.getCompartment();
	}

	/**
	 * Assign the correct species from the species library
	 */
	public void init()
	{
		String species;
		
		if ( this.isAspect(XmlRef.species) )
		{
			species = this.getString(XmlRef.species);
			if (Log.shouldWrite(Tier.DEBUG))
				Log.out(Tier.DEBUG, "Agent belongs to species \""+species+"\"");
		}
		else
		{
			species = "";
			if (Log.shouldWrite(Tier.DEBUG))
				Log.out(Tier.DEBUG, "Agent belongs to void species");
		}
		
		this._aspectRegistry.addSubModule( (Species) 
				Idynomics.simulator.speciesLibrary.get(species));
	}


	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	/**
	 * Allows for direct access to the aspect registry
	 */
	public AspectReg reg()
	{
		return this._aspectRegistry;
	}

	/*
	 * returns object stored in Agent state with name "name". If the state is
	 * not found it will look for the Species state with "name". If this state
	 * is also not found this method will return null.
	 */
	public Object get(String key)
	{
		return _aspectRegistry.getValue(this, key);
	}

	/**
	 * return the compartment the agent is registered to
	 * @return
	 */
	public Compartment getCompartment()
	{
		return this._compartment;
	}

	/**
	 * Set the compartment of this agent.
	 * NOTE: this method should only be called from the compartment when it
	 * is registering a new agent.
	 * @param compartment
	 */
	public void setCompartment(Compartment compartment)
	{
		this._compartment = compartment;
	}

	/*************************************************************************
	 * STEPPING
	 ************************************************************************/

	/**
	 * Perform an event.
	 * @param event
	 */
	public void event(String event)
	{
		this.event(event, null, 0.0);
	}

	public void event(String event, Double timestep)
	{
		this.event(event, null, timestep);
	}

	public void event(String event, Agent compliant)
	{
		this.event(event, compliant, 0.0);
	}

	public void event(String event, Agent compliant, Double timestep)
	{
		this._aspectRegistry.doEvent(this, compliant, timestep, event);
	}

	/*************************************************************************
	 * general methods
	 ************************************************************************/

	/**
	 * \brief Registers the birth of a new agent with the agentContainer.
	 * 
	 * <p>Note that the compartment field of the agent is set by the
	 * compartment itself.</p>
	 */
	public void registerBirth()
	{
		if (Log.shouldWrite(Tier.DEBUG))
			Log.out(Tier.DEBUG, "Compartment \""+this._compartment.name+
					"\" registering agent birth");
		this._compartment.addAgent(this);
		this.set(AspectRef.birthday, Idynomics.simulator.timer.getCurrentTime());
	}

	/**
	 * \brief Registers the death of an agent with the agentContainer.
	 * 
	 * <p>Note that death is different from detachment/dilution/etc, which
	 * instead transfers the agent to another compartment.</p>
	 */
	public void registerDeath()
	{
		// TODO Rob [1June2016]: This method is not yet finished.
		this.set(AspectRef.deathday, Idynomics.simulator.timer.getCurrentTime());
	}
	
	/**
	 * @return The unique identifier of this agent.
	 */
	public int identity()
	{
		return this._uid;
	}
	
	/*************************************************************************
	 * Model Node factory
	 ************************************************************************/
	
	/**
	 * Get the ModelNode object for this Agent object
	 * @return ModelNode
	 */
	@Override
	public Module getModule() 
	{
		/* create the agent node */
		Module modelNode = new Module(XmlRef.agent, this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		/* use the identifier as agent title in gui */
		modelNode.setTitle(String.valueOf(this.identity()));
		
		/* 
		 * store the identity as attribute, note identity cannot be overwritten
		*/
		modelNode.add(new Attribute(XmlRef.identity, 
				String.valueOf(this.identity()), null, false ));
		
		/* add the agents aspects as childNodes */
		for ( String key : this.reg().getLocalAspectNames() )
			modelNode.add(reg().getAspectNode(key));
		
		/* allow adding of new aspects */
		modelNode.addChildSpec( ClassRef.aspect,
				Module.Requirements.ZERO_TO_MANY);
		
		return modelNode;
	}

	public void removeModule(String specifier)
	{
		this._compartment.registerRemoveAgent(this);
	}

	/** 
	 * the default xml label of this class (agent)
	 * @return String XMLtag
	 */
	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.agent;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}
