package agent;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import agent.Species.SpeciesMaker;
import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlLabel;
import generalInterfaces.Quizable;
import generalInterfaces.XMLable;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelMaker.Requirement;

/**
 * \brief Stores information about all species relevant to a simulation.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SpeciesLib implements IsSubmodel, Quizable, XMLable
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
	public void init(Element xmlElem)
	{
		Log.out(Tier.NORMAL, "Species Library loading...");
		/* 
		 * Cycle through all species and add them to the library.
		 */ 
		NodeList nodes = xmlElem.getElementsByTagName(XmlLabel.species);
		String name;
		Element speciesElem;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			speciesElem = (Element) nodes.item(i);
			name = speciesElem.getAttribute(XmlLabel.nameAttribute);
			this.set(name, new Species(speciesElem));
		}
		/* 
		 * Now that all species are loaded, loop through again to find the 
		 * species modules. 
		 */
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			speciesElem = (Element) nodes.item(i);
			name = speciesElem.getAttribute(XmlLabel.nameAttribute);
			Species s = (Species) this._species.get(name);
			Log.out(Tier.EXPRESSIVE,
					"Species \""+name+"\" loaded into Species Library");
			s.loadSpeciesModules(speciesElem);
		}
		Log.out(Tier.NORMAL, "Species Library loaded!\n");
	}

	public String getXml() {
		String out = "<" + XmlLabel.speciesLibrary + ">\n";
		for (String key :_species.keySet())
		{
			out = out + "<" + XmlLabel.species + " name=\"" +
					key + "\">\n" + _species.get(key).getXml() +
					"</" + XmlLabel.species + ">\n";
		}
		out = out + "</" + XmlLabel.speciesLibrary + ">\n";
		return out;
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
		species.reg().identity = name;
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
		if ( this._species.containsKey(name) )
		{
			Log.out(Tier.BULK, "Species Library found \""+name+"\"");
			return this._species.get(name);
		}
		else
		{
			Log.out(Tier.DEBUG, "Species Library could not find \""+name+
					"\", returning void species");
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
}
