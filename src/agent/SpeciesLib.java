package agent;

import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.tier;
import dataIO.XmlLabel;
import generalInterfaces.Quizable;

/**
 * \brief Stores information about all species relevant to a simulation.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SpeciesLib implements Quizable
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
	
	/**
	 * \brief TODO
	 * 
	 * @param xmlElem
	 */
	public void init(Element xmlElem)
	{
		/* Cycle through all species and add them to the library. */ 
		NodeList nodes = xmlElem.getElementsByTagName(XmlLabel.species);
		String name;
		Element speciesElem;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			speciesElem = (Element) nodes.item(i);
			name = speciesElem.getAttribute(XmlLabel.nameAttribute);
			Log.out(tier.EXPRESSIVE, "Loading "+name+" into species library");
			this.set(name, new Species(speciesElem));
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
			Log.out(tier.EXPRESSIVE, "Warning: overwriting species "+name);
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
		return ( this._species.containsKey(name) ) ? 
				this._species.get(name) : this._voidSpecies;
	}
}
