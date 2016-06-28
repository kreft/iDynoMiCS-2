package agent;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.Log;
import dataIO.XmlRef;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Species implements AspectInterface, IsSubmodel, NodeConstructor
{
	/**
	 * TODO
	 */
	protected AspectReg _aspectRegistry = new AspectReg();


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

	@Override
	public String getName()
	{
		return "Species";
	}

	@Override
	public List<InputSetter> getRequiredInputs()
	{
		return new LinkedList<InputSetter>();
	}

	@Override
	public void acceptInput(String name, Object input)
	{
		// TODO
	}

	public static class SpeciesMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -128102479980440674L;

		/**\brief TODO
		 * 
		 * @param name
		 * @param req
		 * @param target
		 */
		public SpeciesMaker(Requirement req, IsSubmodel target)
		{
			super("species", req, target);
		}

		@Override
		protected void doAction(ActionEvent e)
		{
			System.out.println("Making species");
			this.addSubmodel(new Species());
		}
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
		for ( AspectInterface mod : this.reg().getSubModules() )
			modelNode.add(mod.reg().getModuleNode(this));

		/* allow adding of additional aspects */
		modelNode.addChildConstructor(
				this._aspectRegistry.new Aspect(this._aspectRegistry), 
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