package compartment.agentStaging;

import org.w3c.dom.Element;

import agent.Agent;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import utility.Helper;

public abstract class Spawner implements Settable {
	
	private Agent _template;
	
	private int _priority;
	
	private String _compartmentName;

	private Settable _parentNode;
	
	private AgentContainer _agents;
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		this.init(xmlElem,  ((Compartment) parent).agents,
				((Compartment) parent).getName());
	}
	
	public void init(Element xmlElem, AgentContainer agents, 
			String compartmentName)
	{
		this._agents = agents;
		this._compartmentName = compartmentName;
		
		Element p = (Element) xmlElem;
		
		/* spawner priority - default is zero. */
		int priority = 0;
		if ( XmlHandler.hasAttribute(p, XmlRef.processPriority) )
			priority = Integer.valueOf(p.getAttribute(XmlRef.processPriority) );
		this.setPriority(priority);
		
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, defaultXmlTag() + " loaded");
	}

	public void setTemplate(Agent agent)
	{
		this._template = agent;
	}
	
	public Agent getTemplate()
	{
		return _template;
	}
	
	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	public int getPriority()
	{
		return this._priority;
	}
	
	/**
	 * Obtain module for xml output and gui representation.
	 */
	public Module getModule()
	{
		Module modelNode = new Module(defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(defaultXmlTag());
		
		if ( Idynomics.xmlPackageLibrary.has( this.getClass().getSimpleName() ))
			modelNode.add(new Attribute(XmlRef.classAttribute, 
					this.getClass().getSimpleName(), null, false ));
		else
			modelNode.add(new Attribute(XmlRef.classAttribute, 
					this.getClass().getName(), null, false ));
		
		modelNode.add(new Attribute(XmlRef.processPriority, 
				String.valueOf(this._priority), null, true ));
		
		modelNode.addChildSpec( ClassRef.aspect,
				Module.Requirements.ZERO_TO_MANY);
		
		return modelNode;
	}
	
	
	/**
	 * Set value's that (may) have been changed trough the gui.
	 */
	public void setModule(Module node) 
	{
		/* Set the priority */
		this._priority = Integer.valueOf( node.getAttribute( 
				XmlRef.processPriority ).getValue() );
		
		/* Set any potential child modules */
		Settable.super.setModule(node);
	}

	/**
	 * Remove spawner from the compartment
	 * NOTE a bit of a work around but this prevents the spawner form having to 
	 * have access to the compartment directly
	 */
	public void removeModule(String specifier)
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
