package agent;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.AspectInterface;
import aspect.AspectReg;
import aspect.AspectRef;
import dataIO.XmlHandler;
import dataIO.Log;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.Quizable;
import idynomics.Compartment;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import surface.Point;

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
	final int _uid = ++UNIQUE_ID;

	/**
	 * The compartment the agent is currently in
	 */
	protected Compartment _compartment;

	/**
	 * The aspect registry
	 */
	protected AspectReg _aspectRegistry = new AspectReg();

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
	 * Agent xml constructor allowing for multiple randomized initial agents
	 * @param xmlNode
	 */
	// TODO this method needs tidying and clarification
//	public Agent(Node xmlNode, Compartment comp)
//	{
//		
//		NodeList temp = XmlHandler.getAll(xmlNode, "spawn");
//		if ( temp.getLength() > 0 )
//		{
//			/* Initiate all "extra" random agents. */
//			for ( int i = 0; i < temp.getLength(); i++ )
//			{
//				String str;
//				/*
//				 * Find the number of Agents to create.
//				 */
//				str = XmlHandler.obtainAttribute(temp.item(i), "number");
//				int n = Integer.valueOf(str);
//				/*
//				 * Find the domain, i.e. the physical region of space in which
//				 * to randomly place new Agents.
//				 */
//				str = XmlHandler.obtainAttribute(temp.item(i), "domain");
//				double[] domain = Vector.dblFromString(str);
//				/* Create n - 1 agents, as one has already been made. */
//				// TODO give the agents a body shape specified in the protocol
//				// file, rather than assuming it to be coccoid.
//				for ( int j = 0; j < n - 1; j++ )
//				{
//					Agent extra = new Agent(xmlNode);
//					extra.setCompartment(comp);
//					extra.set(NameRef.agentBody, this.randBody(domain));
//					extra.registerBirth();
//				}
//				this.loadAspects(xmlNode);
//				this.set(NameRef.agentBody, this.randBody(domain));
//			}
//		}
//		else
//		{
//			/* No "extra" agents, just this one. */
//			this.loadAspects(xmlNode);
//		}
//		this.init();
//	}
	
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
				int n = Math.round(Float.valueOf(XmlHandler.obtainAttribute(
						temp.item(i), "number")));
				double[] domain = Vector.dblFromString(XmlHandler.
						obtainAttribute(temp.item(i), "domain"));
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
		set(XmlLabel.species,species);
		this._compartment = comp;
		init();
	}

	public Agent(String species, Body body, Compartment comp)
	{
		set(XmlLabel.species, species);
		this.set(AspectRef.agentBody, body);
		this._compartment = comp;
		init();
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
	 * retrieve the current model node
	 */
	@Override
	public ModelNode getNode() 
	{
		/* create the agent node */
		ModelNode modelNode = new ModelNode(XmlLabel.agent, this);
		modelNode.requirement = Requirements.ZERO_TO_MANY;
		
		/* use the identifier as agent title in gui */
		modelNode.title = String.valueOf(this.identity());
		
		/* 
		 * store the identity as attribute, note identity cannot be overwritten
		*/
		modelNode.add(new ModelAttribute(XmlLabel.identity, 
				String.valueOf(this.identity()), null, false ));
		
		// TODO:  add removing aspects
		/* add the agents aspects as childNodes */
		for ( String key : this.reg().getLocalAspectNames() )
			modelNode.add(reg().getAspectNode(key));
		
		/* allow adding of new aspects */
		modelNode.childConstructors.put(reg().new Aspect(reg()), 
				ModelNode.Requirements.ZERO_TO_MANY);
		
		return modelNode;
	}

	/**
	 * update the values of the child nodes (aspects) with the entered values
	 * from the gui
	 */
	@Override
	public void setNode(ModelNode node) 
	{
		for ( ModelNode n : node.childNodes )
			n.constructor.setNode(n);
	}

	/**
	 * create and return a new agent when the add agent button is hit in the
	 * gui
	 */
	@Override
	public NodeConstructor newBlank() 
	{
		Agent newBlank = new Agent(this._compartment);
		newBlank.reg()._identity = String.valueOf(newBlank.identity());
		newBlank.registerBirth();
		return newBlank;
	}

	/** 
	 * the default xml label of this class (agent)
	 */
	@Override
	public String defaultXmlTag() 
	{
		return XmlLabel.agent;
	}


	/*************************************************************************
	 * REPORTING
	 ************************************************************************/

}
