package agent;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.tier;
import dataIO.XmlLoad;
import generalInterfaces.Quizable;

/**
 * The species library maintains a hashmap of all species known in this 
 * simulation
 * @author baco
 *
 */
public class SpeciesLib implements Quizable {
	
	/**
	 * Contains all known species
	 */
	protected HashMap<String, AspectInterface> _species = new HashMap<String, AspectInterface>();
	
	/**
	 * void species, returned if no species is set.
	 */
	protected Species voidSpecies = new Species();
	
	/**
	 * obtains species from Nod.e NOTE: not sure to put it here or in XmlLoad
	 */
	public void setAll(Element speciesNode)
	{
		if(speciesNode != null)
			{
			// cycle trough all species and add them to the species Lib
				NodeList speciesNodes = 
						speciesNode.getElementsByTagName("species");
			
			/*
			 * Loading species aspects
			 */
			for (int i = 0; i < speciesNodes.getLength(); i++) 
			{
				Element xmlSpecies = (Element) speciesNodes.item(i);
				set(xmlSpecies.getAttribute("name"), 
						new Species(speciesNodes.item(i)));
			}
			
			/*
			 * Loading species modules
			 */
			for (int i = 0; i < speciesNodes.getLength(); i++) 
			{
				Element xmlSpecies = (Element) speciesNodes.item(i);
				XmlLoad.loadSpeciesModules( get(
						xmlSpecies.getAttribute("name")),speciesNodes.item(i)); 
			}
		}
		else
		{
			Log.out(tier.EXPRESSIVE, "No species library defined");
		}
	}
	
	public void setAll(Node speciesNode)
	{
		setAll((Element) speciesNode);
	}
	
	/**
	 * Add a new species to the species library (or overwrite if the species
	 * already exists).
	 * @param name
	 * @param spiecies
	 * @return
	 */
	public AspectInterface set(String name, AspectInterface spiecies)
	{
		if ( _species.containsKey(name) )
			System.out.println("Warning: overwriting species module "+name);
		_species.put(name, spiecies);
		return spiecies;
	}
	
	/**
	 * Get a species from the species library
	 * @param name
	 * @return
	 */
	public AspectInterface get(String name)
	{
		if (_species.containsKey(name))
			return _species.get(name);
		else
			return voidSpecies; //return the void species if species is not defined.
	}
}
