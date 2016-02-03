package agent;

import java.util.HashMap;

/**
 * The species library maintains a hashmap of all species known in this 
 * simulation
 * @author baco
 *
 */
public class SpeciesLib {

	
	/**
	 * Contains all known species
	 */
	protected static HashMap<String, Species> _species = new HashMap<String, Species>();
	
	/**
	 * void species, returned if no species is set.
	 */
	protected static Species voidSpecies = new Species();
	
	/**
	 * Add a new species to the species library (or overwrite if the species
	 * already exists).
	 * @param name
	 * @param spiecies
	 * @return
	 */
	public static Species set(String name, Species spiecies)
	{
		if ( SpeciesLib._species.containsKey(name) )
			System.out.println("Warning: overwriting species module "+name);
		SpeciesLib._species.put(name, spiecies);
		return spiecies;
	}
	
	/**
	 * Get a species from the species library
	 * @param name
	 * @return
	 */
	public static Species get(String name)
	{
		if (_species.containsKey(name))
			return _species.get(name);
		else
			return voidSpecies; //return the void species if species is not defined.
	}
}
