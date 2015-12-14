package agent;

import java.util.HashMap;

public class CladeLib {

	protected static HashMap<String, Clade> _clades = new HashMap<String, Clade>();
	protected static Clade voidClade = new Clade();
	
	public static Clade set(String name, Clade spiecies)
	{
		if ( CladeLib._clades.containsKey(name) )
			System.out.println("Warning: overwriting clade "+name);
		CladeLib._clades.put(name, spiecies);
		return spiecies;
	}
	
	public static Clade get(String name)
	{
		if (_clades.containsKey(name))
			return _clades.get(name);
		else
			return voidClade; //return the void clade if clade is not defined.
	}
}
