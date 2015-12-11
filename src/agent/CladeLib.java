package agent;

import java.util.HashMap;

public class CladeLib {

	protected static HashMap<String, Clade> _species = new HashMap<String, Clade>();
	protected static Clade voidSpecies = new Clade();
	
	public static Clade set(String name, Clade spiecies)
	{
		if ( CladeLib._species.containsKey(name) )
			System.out.println("Warning: overwriting clade "+name);
		CladeLib._species.put(name, spiecies);
		return spiecies;
	}
	
	public static Clade get(String name)
	{
		if (_species.containsKey(name))
			return _species.get(name);
		else
			return voidSpecies; //return a void species if species is not defined.
	}
}
