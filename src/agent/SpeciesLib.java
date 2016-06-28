package agent;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Species.SpeciesMaker;
import aspect.AspectInterface;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import generalInterfaces.Instantiatable;
import idynomics.Idynomics;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelMaker.Requirement;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;

/**
 * \brief Stores information about all species relevant to a simulation.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class SpeciesLib implements IsSubmodel, Instantiatable, NodeConstructor
{
	/**
	 * Contains all known species.
	 */
	protected HashMap<String, AspectInterface> _species = 
			new HashMap<String, AspectInterface>();

	/**
	 * Void species, returned if no species is set.
	 */
	protected Species _voidSpecies = new Species();

	public String[] getAllSpeciesNames()
	{
		String[] names = new String[_species.size()];
		int i = 0;
		for(String name : _species.keySet())
		{
			names[i] = name;
			i++;
		}
		return names;
	}

	/**
	 * \brief TODO
	 * 
	 * @param xmlElem
	 */
	public void init(Element xmlElem, NodeConstructor parent)
	{
		Log.out(Tier.NORMAL, "Species Library loading...");
		/* 
		 * Cycle through all species and add them to the library.
		 */ 
		NodeList nodes = xmlElem.getElementsByTagName(XmlRef.species);
		String name;
		Element speciesElem;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			speciesElem = (Element) nodes.item(i);
			name = speciesElem.getAttribute(XmlRef.nameAttribute);
			this.set(name, new Species(speciesElem));
		}
		/* 
		 * Now that all species are loaded, loop through again to find the 
		 * species modules. 
		 */
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			speciesElem = (Element) nodes.item(i);
			name = speciesElem.getAttribute(XmlRef.nameAttribute);
			Species s = (Species) this._species.get(name);
			Log.out(Tier.EXPRESSIVE,
					"Species \""+name+"\" loaded into Species Library");
			this.loadSpeciesModules(speciesElem, s);
		}
		Log.out(Tier.NORMAL, "Species Library loaded!\n");
	}
	
	public void loadSpeciesModules(Node xmlElem, Species species)
	{
		NodeList nodes = XmlHandler.getAll(xmlElem, XmlRef.speciesModule);
		String name;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			Element s = (Element) nodes.item(i);
			name = s.getAttribute(XmlRef.nameAttribute);
			Log.out(Tier.DEBUG, "Loading SpeciesModule \""+name+"\"");
			species._aspectRegistry.addSubModule(
					this.get(name) );
		}
	}

	/**
	 * \brief Add a new species to the species library (or overwrite if the
	 * species already exists).
	 * 
	 * @param name Species name.
	 * @param species Information about the species.
	 */
	public void set(String name, AspectInterface species)
	{
		if ( this._species.containsKey(name) )
			Log.out(Tier.EXPRESSIVE, "Warning: overwriting species "+name);
		species.reg().setIdentity(name);
		this._species.put(name, species);
	}
	
	/**
	 * \brief Add a new species to the species library using the interface
	 * (or overwrite if the species already exists).
	 * 
	 * @param species Information about the species.
	 */
	public void set(AspectInterface species)
	{
		String name = species.reg().getIdentity();
		if ( this._species.containsKey(name) )
			Log.out(Tier.EXPRESSIVE, "Warning: overwriting species "+name);
		species.reg().setIdentity(name);
		this._species.put(name, species);
	}

	/**
	 * \brief Get a species from the species library.
	 * 
	 * @param name Species name.
	 * @return Information about the species if it is found. If it cannot be
	 * found, returns the void species instead.
	 */
	public AspectInterface get(String name)
	{
		Tier level;
		if ( this._species.containsKey(name) )
		{
			level = Tier.BULK;
			if ( Log.shouldWrite(level) )
				Log.out(level, "Species Library found \""+name+"\"");
			return this._species.get(name);
		}
		else
		{
			level = Tier.DEBUG;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "Species Library could not find \""+name+
						"\", returning void species");
			}
			return this._voidSpecies;
		}
	}

	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/

	public String getName()
	{
		return "Species Library";
	}

	public List<InputSetter> getRequiredInputs()
	{
		// TODO implement species
		List<InputSetter> out = new LinkedList<InputSetter>();
		out.add(new SpeciesMaker(Requirement.ZERO_TO_MANY, this));
		return out;
	}

	public void acceptInput(String name, Object input)
	{
		if ( input instanceof Species )
		{
			this._species.put(name, (Species) input);
			// TODO void species?
		}
	}

	public static class SpeciesLibMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -6601262340075573910L;

		public SpeciesLibMaker(Requirement req, IsSubmodel target)
		{
			super("species library", req, target);
		}

		@Override
		public void doAction(ActionEvent e)
		{
			System.out.println("Making speciesLib");
			this.addSubmodel(new SpeciesLib());
		}
	}

	/**
	 * Get the ModelNode object for this NodeConstructor object
	 * @return ModelNode
	 */
	@Override
	public ModelNode getNode() {

		/* the species lib node */
		ModelNode modelNode = new ModelNode(XmlRef.speciesLibrary, this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		
		/* Species constructor */
		modelNode.addChildConstructor(new Species(), 
				ModelNode.Requirements.ZERO_TO_MANY);
		
		/* the already existing species */
		for ( String s : this._species.keySet() )
			modelNode.add(((Species) _species.get(s)).getNode());
	
		return modelNode;
	}

	/**
	 * Create a new minimal object of this class and return it
	 * @return NodeConstructor
	 */
	@Override
	public NodeConstructor newBlank() {
		return Idynomics.simulator.speciesLibrary;
	}

	/**
	 * Add a child object that is unable to register itself properly via the
	 * newBlank call.
	 * @param childOb
	 */
	@Override
	public void addChildObject(NodeConstructor childObject) 
	{
		if (childObject instanceof Species)
			this.set((Species) childObject);
	}

	/**
	 * return the default XMLtag for the XML node of this object
	 * @return String xmlTag
	 */
	@Override
	public String defaultXmlTag() {
		return XmlRef.speciesLibrary;
	}

}
