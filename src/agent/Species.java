package agent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.AspectReg;
import generalInterfaces.Instantiatable;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.primarySetters.LinkedListSetter;
import nodeFactory.primarySetters.Pile;
import referenceLibrary.ClassRef;
import referenceLibrary.ObjectRef;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Species implements AspectInterface, NodeConstructor, Instantiatable
{
	/**
	 * TODO
	 */
	protected AspectReg _aspectRegistry = new AspectReg();
	/**
	 * 
	 */
	protected NodeConstructor _parentNode;
	
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
	
	public void init(Element xmlElem, NodeConstructor parent)
	{
		//TODO currently only accounting for gui initiation, use constructor for other.
		this._parentNode = parent;
		String name = "";
		name = Helper.obtainInput(name, "Species name");
		this.reg().setIdentity(name);
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
	public ModelNode getNode() 
	{
		/* the species node */
		ModelNode modelNode = new ModelNode(XmlRef.species, this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		/* use the identity (species name) as title */
		modelNode.setTitle(this.reg().getIdentity());
		
		/* add the name attribute */
		modelNode.add(new ModelAttribute(XmlRef.nameAttribute, 
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
		modelNode.add(this.reg().getSubModuleNames().getNode());

		/* allow adding of additional aspects */
		/* allow adding of new aspects */
		modelNode.addConstructable( ClassRef.aspect,
				ModelNode.Requirements.ZERO_TO_MANY);
		
		/* TODO: removing aspects */
		
		/* add already existing aspects */
		for ( String key : this.reg().getLocalAspectNames() )
			modelNode.add(reg().getAspectNode(key));
		
		return modelNode;
	}

	/**
	 * Create a new minimal object of this class and return it, used by the gui
	 * to add new Species
	 * @return NodeConstructor
	 */
	@Override
	public NodeConstructor newBlank() 
	{
		String name = "";
		name = Helper.obtainInput(name, "Species name");
		Species newBlank = new Species();
		newBlank.reg().setIdentity(name);
//		Idynomics.simulator.speciesLibrary.set(newBlank);
		return newBlank;
	}
	

	@Override
	public void removeNode(String specifier) 
	{
		if ( specifier == this.reg().getIdentity() )
			Idynomics.simulator.speciesLibrary._species.remove(this.reg().getIdentity());
		else
			this.reg().removeSubmodule(specifier);
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

}