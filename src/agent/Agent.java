package agent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.AspectInterface;
import aspect.AspectReg;
import aspect.AspectReg.Aspect;
import dataIO.XmlHandler;
import dataIO.Log;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.Quizable;
import idynomics.Compartment;
import idynomics.Idynomics;
import idynomics.NameRef;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import surface.Point;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Agent implements Quizable, AspectInterface, NodeConstructor
{
	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
	final int uid = ++UNIQUE_ID;

	/**
	 * The compartment the agent is currently in
	 */
	protected Compartment compartment;
	
	protected ModelNode modelNode;

	/**
	 * The aspect registry
	 */
	public AspectReg aspectRegistry = new AspectReg();

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	public Agent()
	{

	}
	
	/* used by gui, dummy agent */
	public Agent(Compartment comp)
	{
		this.compartment = comp;
	}

	/**
	 * @deprecated
	 * @param xmlNode
	 */
	public Agent(Node xmlNode)
	{
		this(xmlNode, new Compartment());
	

	}

	/**
	 * Agent xml constructor allowing for multiple randomized initial agents
	 * @param xmlNode
	 */
	public Agent(Node xmlNode, Compartment comp)
	{
		/* initiate all random agents */
		NodeList temp = XmlHandler.getAll(xmlNode, "spawn");
		if(temp.getLength() > 0)
		{
			for(int i = 0; i < temp.getLength(); i++)
			{
				/* TODO this is a cheat, make a standard method for this */
				int n = Math.round(Float.valueOf(XmlHandler.obtainAttribute((Element) 
						temp.item(i), "number")));
				double[] domain = Vector.dblFromString(XmlHandler.
						obtainAttribute((Element) temp.item(i), "domain"));
				for(int j = 0; j < n-1; j++)
				{
					Agent extra = new Agent(xmlNode, randBody(domain));
					extra.compartment = comp;
					extra.registerBirth();
				}
				this.loadAspects(xmlNode);
				this.set(NameRef.agentBody, randBody(domain));
			}
		}
		else
		{
			loadAspects(xmlNode);
		}
		this.init();
	}

	public Body randBody(double[] domain)
	{
		return new Body(new Point(Vector.
				times(Vector.randomZeroOne(domain), domain)), 0.0);
	}

	public Agent(Node xmlNode, Body body)
	{
		this.loadAspects(xmlNode);
		this.set(NameRef.agentBody, body);
		this.init();
	}

	public Agent(String species, Compartment comp)
	{
		set(XmlLabel.species,species);
		this.compartment = comp;
		init();
	}

	public Agent(String species, Body body, Compartment comp)
	{
		set(XmlLabel.species, species);
		this.set(NameRef.agentBody, body);
		this.compartment = comp;
		init();
	}

	/**
	 * NOTE: this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		this.aspectRegistry.duplicate(agent);
		this.init();
		this.compartment = agent.getCompartment();
	}

	/**
	 * Assign the correct species from the species library
	 */
	public void init()
	{
		String species;
		if ( this.isAspect(XmlLabel.species) )
		{
			species = this.getString(XmlLabel.species);
			Log.out(Tier.DEBUG, "Agent belongs to species \""+species+"\"");
		}
		else
		{
			species = "";
			Log.out(Tier.DEBUG, "Agent belongs to void species");
		}
		aspectRegistry.addSubModule( (Species) 
				Idynomics.simulator.speciesLibrary.get(species));
	}


	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	/**
	 * Allows for direct access to the aspect registry
	 */
	public AspectReg reg() {
		return aspectRegistry;
	}

	/*
	 * returns object stored in Agent state with name "name". If the state is
	 * not found it will look for the Species state with "name". If this state
	 * is also not found this method will return null.
	 */
	public Object get(String key)
	{
		return aspectRegistry.getValue(this, key);
	}

	/**
	 * return the compartment the agent is registered to
	 * @return
	 */
	public Compartment getCompartment()
	{
		return compartment;
	}

	/**
	 * Set the compartment of this agent.
	 * NOTE: this method should only be called from the compartment when it
	 * is registering a new agent.
	 * @param compartment
	 */
	public void setCompartment(Compartment compartment)
	{
		this.compartment = compartment;
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
		event(event, null, 0.0);
	}

	public void event(String event, Double timestep)
	{
		event(event, null, timestep);
	}

	public void event(String event, Agent compliant)
	{
		event(event, compliant, 0.0);
	}

	public void event(String event, Agent compliant, Double timestep)
	{
		aspectRegistry.doEvent(this,compliant,timestep,event);
	}

	/*************************************************************************
	 * general methods
	 ************************************************************************/

	/**
	 * \brief: Registers the birth of a new agent with the agentContainer.
	 * note that the compartment field of the agent is set by the compartment
	 * itself.
	 */
	public void registerBirth()
	{
		Log.out(Tier.DEBUG, "Compartment \""+this.compartment.name+
				"\" registering agent birth");
		compartment.addAgent(this);
	}

	/**
	 * return the unique identifier of the agent.
	 * @return
	 */
	public int identity() {
		return uid;
	}
	
	@Override
	public ModelNode getNode() 
	{
		if(modelNode == null)
		{
			modelNode = new ModelNode(XmlLabel.agent, this);
			modelNode.requirement = Requirements.ZERO_TO_MANY;
			modelNode.title = String.valueOf(this.identity());
			
			modelNode.add(new ModelAttribute("identity", 
					String.valueOf(this.identity()), null, false ));
			
			/* TODO: add aspects */
			
			for ( String key : this.reg().getLocalAspectNames() )
				modelNode.add(reg().getAspectNode(key));
			
			modelNode.childConstructors.put(reg().new Aspect(reg()), 
					ModelNode.Requirements.ZERO_TO_MANY);
			
		}
		return modelNode;
	}

	@Override
	public void setNode(ModelNode node) 
	{
		for(ModelNode n : node.childNodes)
			n.constructor.setNode(n);
	}

	@Override
	public NodeConstructor newBlank() 
	{
		Agent newBlank = new Agent(this.compartment);
		newBlank.reg().identity = String.valueOf(newBlank.identity());
		newBlank.registerBirth();
		return newBlank;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) 
	{
		// TODO 
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlLabel.agent;
	}


	/*************************************************************************
	 * REPORTING
	 ************************************************************************/

}
