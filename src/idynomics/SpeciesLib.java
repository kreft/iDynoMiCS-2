package idynomics;

import java.util.HashMap;

import agent.Species;

public class SpeciesLib {

	protected static HashMap<String, Species> _species = new HashMap<String, Species>();
	
	public static Species set(String name, Species spiecies)
	{
		if ( SpeciesLib._species.containsKey(name) )
			System.out.println("Warning: overwriting species "+name);
		SpeciesLib._species.put(name, spiecies);
		return spiecies;
	}
	
	public static Species get(String name)
	{
		if (_species.containsKey(name))
			return _species.get(name);
		else
			return new Species(); //return a void species if species is not defined.
	}
}
