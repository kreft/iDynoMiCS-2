package agent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;
import idynomics.Idynomics;
import instantiable.Instantiable;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Species implements AspectInterface, Settable, Instantiable
{
	/**
	 * TODO
	 */
	protected AspectReg _aspectRegistry = new AspectReg();
	/**
	 * 
	 */
	protected Settable _parentNode;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	/**
	 * \brief TODO
	 *
	 */
	public Species()
	{
		// Do nothing!
	}

	/**
	 * \brief TODO
	 * 
	 * @param xmlNode
	 */
	public Species(Node xmlNode)
	{
		/* Load the primary aspects of this Species. */
		this.loadAspects(xmlNode);
		this._parentNode = Idynomics.simulator.speciesLibrary;
	}
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		//TODO currently only accounting for gui initiation, use constructor for other.
		this._parentNode = parent;
		String name = "";
		name = Helper.obtainInput(name, "Species name");
		this.reg().setIdentity(name);
		Idynomics.simulator.speciesLibrary.addSpecies(this);
	}

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	/**
	 * Get this {@code Species}' aspect registry.
	 */
	public AspectReg reg()
	{
		return this._aspectRegistry;
	}

	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/

	
	public String getName()
	{
		return "Species";
	}

	/**
	 * Get the ModelNode object for this Species
	 * @return ModelNode
	 */
	@Override
	public Module getModule() 
	{
		/* the species node */
		Module modelNode = new Module(XmlRef.species, this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		/* use the identity (species name) as title */
		modelNode.setTitle(this.reg().getIdentity());
		
		/* add the name attribute */
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this.reg().getIdentity(), null, true ));
		
		/* add any submodules */
//		for ( AspectInterface mod : this.reg().getSubModules() )
//			modelNode.add(mod.reg().getModuleNode(this));
		
//		for ( AspectInterface mod : this.reg().getSubModules() )
//			modelNode.add(new LinkedListSetter<String>(
//					mod.reg().getIdentity(), this.reg().getSubModuleNames(),
//					ObjectRef.STR, XmlRef.nameAttribute,
//					XmlRef.speciesModule ).getNode() );
		
//		Pile<String> nodes = new Pile<String>(XmlRef.nameAttribute, "submodules", XmlRef.speciesModule);
//		nodes.addAll(this.reg().getSubModuleNames());
		this.reg().getSubModuleNames().requirement = Requirements.IMMUTABLE;
		this.reg().getSubModuleNames().muteAttributeDef = true;
//		nodes.muteClassDef = true;
		modelNode.add(this.reg().getSubModuleNames().getModule());

		/* allow adding of additional aspects */
		/* allow adding of new aspects */
		modelNode.addChildSpec( ClassRef.aspect,
				Module.Requirements.ZERO_TO_MANY);

		/* add already existing aspects */
		for ( String key : this.reg().getLocalAspectNames() )
			modelNode.add(reg().getAspectNode(key));
		
		return modelNode;
	}

	@Override
	public void removeModule(String specifier) 
	{
		if ( specifier == this.reg().getIdentity() )
			Idynomics.simulator.speciesLibrary._species.remove(this.reg().getIdentity());
		else
			this.reg().removeModule(specifier);
	}

	/**
	 * return the default XMLtag for the XML node of this object
	 * @return String xmlTag
	 */
	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.species;
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