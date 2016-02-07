package idynomics;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import dataIO.XmlHandler;

/**
 * library used to store packages associated with common classes, allows for
 * quick class assignment in xml files
 * @author baco
 *
 */
public class XMLableLibrary {

	/*
	 * Hashmap that stores all Class,package associations
	 */
	private HashMap<String, String> _lib = new HashMap<String, String>();
	
	public XMLableLibrary()
	{
		Element classLibrary = 
				XmlHandler.loadDocument("general/classLibrary.xml");
		List<String[]> tempLib = XmlHandler.gatherAtributes( classLibrary, 
				"classDef", new String[]{"name", "package"});
		for(String[] c : tempLib)
			set(c[0], c[1]);
	}
	
	/*
	 * Retrieve package name from class
	 */
	public String get(String key)
	{
		if(_lib.containsKey(key))
			return _lib.get(key);
		else
		{
			Feedback.out(LogLevel.CRITICAL, "Could not obtain " + key + " from"
					+ " XMLableLibrary");
			return null;
		}
	}
	
	/*
	 * class sets className , package associations
	 */
	public void set(String className, String classPackage)
	{
		if(_lib.containsKey(className))
		{
			Feedback.out(LogLevel.DEBUG, "Overwriting class,package association"
					+ className + " , " + classPackage);
		}
		_lib.put(className, classPackage);
	}
}
