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
import dataIO.XmlLabel;
import dataIO.Log.Tier;
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
	

	private ModelNode modelNode;

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

	public void loadSpeciesModules(Element xmlElem)
	{
		NodeList nodes = xmlElem.getElementsByTagName(XmlLabel.speciesModule);
		String name;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			Element s = (Element) nodes.item(i);
			name = s.getAttribute(XmlLabel.nameAttribute);
			Log.out(Tier.DEBUG, "Loading SpeciesModule \""+name+"\"");
			this._aspectRegistry.addSubModule(name, 
					Idynomics.simulator.speciesLibrary);
		}
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

	@Override
	public ModelNode getNode() 
	{
		if(modelNode == null)
		{
			modelNode = new ModelNode(XmlLabel.species, this);
			modelNode.requirement = Requirements.ZERO_TO_MANY;
			modelNode.title = this.reg().identity;
			
			modelNode.add(new ModelAttribute(XmlLabel.nameAttribute, 
					this.reg().identity, null, true ));
			
			for ( AspectInterface mod : this.reg().getSubModules() )
			{
				modelNode.add(mod.reg().getModuleNode(this));
			}

			modelNode.childConstructors.put(_aspectRegistry.new Aspect(_aspectRegistry), 
					ModelNode.Requirements.ZERO_TO_MANY);
			
			/* TODO: add aspects */
			
			for ( String key : this.reg().getLocalAspectNames() )
				modelNode.add(reg().getAspectNode(key));
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
		
		String name = "";
		name = Helper.obtainInput(name, "Species name");
		Species newBlank = new Species();
		newBlank.reg().identity = name;
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
		return XmlLabel.species;
	}
}