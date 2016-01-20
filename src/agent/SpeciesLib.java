package agent;

import java.util.HashMap;

public class SpeciesLib {

	protected static HashMap<String, Species> _species = new HashMap<String, Species>();
	protected static Species voidSpecies = new Species();
	
	public static Species set(String name, Species spiecies)
	{
		if ( SpeciesLib._species.containsKey(name) )
			System.out.println("Warning: overwriting species module "+name);
		SpeciesLib._species.put(name, spiecies);
		return spiecies;
	}
	
	public static Species get(String name)
	{
		if (_species.containsKey(name))
			return _species.get(name);
		else
			return voidSpecies; //return the void species if species is not defined.
	}
}
