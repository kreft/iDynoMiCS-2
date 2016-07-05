package idynomics;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import referenceLibrary.ClassRef;
import utility.Helper;

/**
 * \brief Library used to store packages associated with common classes, allows
 * for quick class assignment in XML files.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class XMLableLibrary
{
	/**
	 * Hashmap that stores all Class,package associations
	 */
	private HashMap<String, String> _lib = new HashMap<String, String>();
	
	/**
	 * \brief TODO
	 */
	public XMLableLibrary()
	{
//		Element classLibrary = 
//				XmlHandler.loadResource("/general/classLibrary.xml");
//		List<String[]> tempLib = XmlHandler.gatherAtributesFrom( classLibrary, 
//				"classDef", new String[]{"name", "package"});
		String[] tempLib = ClassRef.getAllOptionsFullPath();
		for ( String c : tempLib )
			this.set(ClassRef.simplify(c), ClassRef.path(c));
	}

	/**
	 * \brief Retrieve package name from class.
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key)
	{
		key = Helper.firstToUpper(key);
		if ( this.has(key) )
			return this._lib.get(key);
		else
		{
			Log.out(Tier.CRITICAL, 
						"Could not obtain " + key + " from XMLableLibrary");
			return null;
		}
	}
	
	/**
	 * List all known classes (from classLibrary.xml) in given package
	 * @param PackageDefinition
	 * @return
	 */
	public List<String> getAll(String PackageDefinition)
	{
		List<String> out = new LinkedList<String>();
		for ( String key : this._lib.keySet() )
			if ( get(key).equals(PackageDefinition) )
				out.add(key);
		return out;
	}
	
	/**
	 * return false if XMLableLibrary does not contain key.
	 * @param key
	 * @return
	 */
	public boolean has(String key)
	{
		return this._lib.containsKey(key);
	}
	
	/**
	 * class sets className , package associations
	 */
	public void set(String className, String classPackage)
	{
		if ( this._lib.containsKey(className) )
		{
			Log.out(Tier.DEBUG, "Overwriting class,package association"
					+ className + " , " + classPackage);
		}
		this._lib.put(className, classPackage);
	}
}
