package compartment.agentStaging;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body.Morphology;
import compartment.AgentContainer;
import compartment.Compartment;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import instantiable.Instantiable;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import surface.BoundingBox;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public abstract class Spawner implements Settable, Instantiable {
	
	protected Agent _template;
	
	protected int _numberOfAgents;
	
	protected int _priority;
	
	protected Compartment _compartment;

	protected Settable _parentNode;
	
	protected Morphology morphology;
	
	/**
	 * BoundingBox for spawn domain
	 * TODO maybe this can be more generally applied and we should move this to
	 * the Spawner super class.
	 */
	protected BoundingBox spawnDomain = new BoundingBox();
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		this.init(xmlElem,  ((Compartment) parent).agents,
				((Compartment) parent).getName());
	}
	
	public void init(Element xmlElem, AgentContainer agents, 
			String compartmentName)
	{
		this.setCompartment(
				Idynomics.simulator.getCompartment(compartmentName) );
		
		Element p = (Element) xmlElem;
		
		if ( XmlHandler.hasAttribute(p, XmlRef.spawnDomain) )
		{
			double[][] input = 
					Matrix.dblFromString(p.getAttribute(XmlRef.spawnDomain));
			if( Matrix.rowDim(input) < 2)
				spawnDomain.get(input[0], Vector.zeros(input[0]));
			else
				spawnDomain.get(input[0], input[1]);
		}
		
		/* spawner priority - default is zero. */
		int priority = 0;
		if ( XmlHandler.hasAttribute(p, XmlRef.priority) )
			priority = Integer.valueOf(p.getAttribute(XmlRef.priority) );
		this.setPriority(priority);
		
		if ( XmlHandler.hasAttribute(p, XmlRef.numberOfAgents) )
			this.setNumberOfAgents( Integer.valueOf(
					p.getAttribute(XmlRef.numberOfAgents) ) );
		
		if ( XmlHandler.hasAttribute(p, XmlRef.morphology) )
			this.setMorphology( Morphology.valueOf(
					p.getAttribute(XmlRef.morphology) ) );
		
		Element template = XmlHandler.findUniqueChild(xmlElem, 
				XmlRef.templateAgent);
		/* using template constructor */
		this.setTemplate( new Agent( template, true ) );
		
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
	
	public int getNumberOfAgents() 
	{
		return _numberOfAgents;
	}

	public void setNumberOfAgents(int _numberOfAgents) 
	{
		this._numberOfAgents = _numberOfAgents;
	}

	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	public int getPriority()
	{
		return this._priority;
	}
	
	public abstract void spawn();
	
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
		
		modelNode.add(new Attribute(XmlRef.priority, 
				String.valueOf(this._priority), null, true ));
		
		modelNode.add(new Attribute(XmlRef.numberOfAgents, 
				String.valueOf(this.getNumberOfAgents()), null, true ));
		
		modelNode.add(new Attribute(XmlRef.morphology, 
				String.valueOf(this.getMorphology()), null, true ));
		
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
		Idynomics.simulator.deleteFromCompartment(this.geCompartment().getName(), this);
	}

	/**
	 * 
	 */
	public String defaultXmlTag() 
	{
		return XmlRef.spawnNode;
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

	public Morphology getMorphology() {
		return morphology;
	}

	public void setMorphology(Morphology morphology) {
		this.morphology = morphology;
	}

	public Compartment geCompartment() {
		return _compartment;
	}

	public void setCompartment(Compartment _compartment) {
		this._compartment = _compartment;
	}
}
