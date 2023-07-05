package agent;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.Aspect.AspectClass;
import aspect.AspectInterface;
import aspect.AspectReg;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import surface.Point;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Agent implements AspectInterface, Settable, Instantiable
{
	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 * 
	 * becomiming tricky consider redesign
	 */
	protected static int UNIQUE_ID = 0;
	protected int _uid;

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
	 * NOTE Bit of a hack allows initiation of multiple random agents
	 * Agent xml constructor allowing for multiple randomized initial agents
	 * @param xmlNode
	 */
	public Agent(Node xmlNode, Compartment comp)
	{
		this.init(xmlNode, comp);
	}
	
	public Agent (Node xmlNode, Body body, Compartment comp) {
		Agent extra = new Agent(xmlNode, body);
		extra._compartment = comp;
		extra.registerBirth();
	}
	
	public void init(Node xmlNode, Compartment comp)
	{
		this._compartment = comp;
		/* initiate all random agents */
		NodeList temp = XmlHandler.getAll(xmlNode, XmlRef.spawnNode);
		if(temp.getLength() > 0)
		{
			// Spawn random agents
			for(int i = 0; i < temp.getLength(); i++)
			{
				/* NOTE this remains here for older protocols, for newer ones we
				 * should use the spawner classes. */
				int n = Integer.valueOf(XmlHandler.obtainAttribute(
						temp.item(i), XmlRef.numberOfAgents, 
						this.defaultXmlTag() ) );
				double[] domain = Vector.dblFromString(
						XmlHandler.obtainAttribute(temp.item(i), 
						XmlRef.spawnDomain, this.defaultXmlTag() ) );
				int points = Integer.valueOf( Helper.setIfNone(  
						XmlHandler.gatherAttribute(temp.item(i), XmlRef.points) 
						, "1" ) );
				for(int j = 0; j < n-1; j++)
				{
					Agent extra = new Agent(xmlNode, randBody(domain, points));
					extra._compartment = comp;
					extra.registerBirth();
				}
				this.loadAspects(xmlNode);
				this.set(AspectRef.agentBody, randBody(domain, points));
			}
		}
		else
		{
			String in =  XmlHandler.gatherAttribute(xmlNode, 
					XmlRef.identity);
			if( in == null)
				this.number(null);
			else
				this.number(Integer.valueOf(in));
			// Place located agents
			loadAspects(xmlNode);
		}
		this.initiate();
	}
	
	private void number(Integer in)
	{
		if(in != null)
		{
			if ( UNIQUE_ID <= in )
				UNIQUE_ID++;
			if( Idynomics.simulator.active() || //IMPORTANT prevent searching for duplicate number assignment in running simulations for efficiency.
					Idynomics.simulator.findAgent(Integer.valueOf(in)) == null )
				this._uid = Integer.valueOf(in);
			else
			{
				Log.out(Tier.NORMAL, "attempted to assign pre-existing agent"+
						" identity, assigning next instead");
				number(null);
			}
		}
		else
			number( UNIQUE_ID );
	}
	
	/**
	 * Assign the correct species from the species library
	 */
	public void initiate()
	{
		if( this._uid == 0)
			this.number(null);
		String species;
		species = this.getString(XmlRef.species);
		this._aspectRegistry.addModule( (Species) 
				Idynomics.simulator.speciesLibrary.get(species), species);
	}
	
	/**
	 * Instantiatable implementation
	 */
	public void instantiate(Element xmlElement, Settable parent)
	{
		((Compartment) parent.getParent()).addAgent(this);
		this._compartment = (Compartment) parent.getParent();
		loadAspects(xmlElement);
		this.initiate();
	}
		
	/* FIXME work in progress */
	@Deprecated
	public Body randBody(double[] domain, int p)
	{
		double[] v = Vector.randomZeroOne(domain);
		Vector.timesEquals(v, domain);
		List<Point> points = new LinkedList<Point>();
		points.add(new Point(v));
		for ( int i = 1; i < p; i++ )
			points.add(new Point(Vector.add(v,Vector.randomZeroOne(domain))));
		return new Body(points, 0, 0);
	}

	public Agent(Node xmlNode, Body body)
	{
		this.loadAspects(xmlNode);
		this.set(AspectRef.agentBody, body);
		this.initiate();
	}
	
	/**
	 * template constructor
	 * @param xmlNode
	 * @param boo
	 */
	public Agent(Node xmlNode, boolean boo)
	{
		this.loadAspects(xmlNode);
		this.initiate();
	}

	public Agent(String species, Compartment comp)
	{
		this.set(XmlRef.species, species);
		this._compartment = comp;
		this.initiate();
	}

	public Agent(String species, Body body, Compartment comp)
	{
		this.set(XmlRef.species, species);
		this.set(AspectRef.agentBody, body);
		this._compartment = comp;
		this.initiate();
	}

	/**
	 * NOTE: this is a copy constructor, keep up to date, make deep copies
	 * uid is the unique identifier and should always be unique
	 * @param agent
	 */
	public Agent(Agent agent)
	{
		this._aspectRegistry.duplicate(agent);
		this._compartment = agent.getCompartment();
		this.initiate();
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
	
	public AspectClass getAspectType(String key)
	{
		return reg().getType(this, key);
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
	
	
	/**
	 * Agent's with only one point will have their location co-ordinates
	 * simplified to zeros. Agent's with multiple points will have each 
	 * co-ordinate reduced by the lowest value in that dimension.
	 * 
	 * Review whether this is useful
	 */
	public void simplifyLocation()
	{
		if (this.isLocalAspect(XmlRef.agentBody))
			{
				Body body = (Body) this._aspectRegistry.getValue(
						this, XmlRef.agentBody);
				body.simplifyLocation();
			}
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
	
	/**
	 * Update this module and all child modules with updated information from
	 * the gui (init if the agent is newly created in the gui).
	 */
	public void setModule(Module node)
	{
		Settable.super.setModule(node);
		this.initiate(); // Updates species module if changed
	}

	/**
	 * respond to gui command to remove the agent
	 */
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
		//this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._compartment;
	}
}
