/**
 * 
 */
package generalInterfaces;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import dataIO.LogFile;
import idynomics.Idynomics;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author baco
 */
public interface XMLable
{
	/**
	 * \brief External method for creating a new instance.
	 * 
	 * <p>Remember to typecast when using this. E.g.,</p>
	 * <p>{@code this.thing = (Thing) Thing.getNewInstance(className);}.</p>
	 * 
	 * <p><b>IMPORTANT:</b> This method should only be overwritten in the class
	 * that implements XMLable if a prefix is necessary.</p>
	 * 
	 * @param className {@code String} name of the class to be instanciated.
	 * This method will ensure that the first letter is in upper case, but only
	 * the first!
	 * @return A new instance of the class required.
	 */
	public static Object getNewInstance(String className)
	{
		return getNewInstance(className, null);
	}
	
	/**
	 * \brief Internal method for creating a new instance.
	 * 
	 * <p><b>IMPORTANT:</b> This method should only be called by the class that
	 * implements XMLable.</p>
	 * 
	 * @param className {@code String} name of the class to be instantiated.
	 * This method will ensure that the first letter is in upper case, but only
	 * the first!
	 * @param prefix {@code String} prefix to <b>className</b>. Typical format:
	 * "packageName.ClassLibrary$".
	 * @return A new instance of the class required.
	 */
	public static Object getNewInstance(String className, String prefix)
	{
		/*
		 * Check the first letter is upper case.
		 */
		String firstLetter = className.substring(0, 1);
		if ( firstLetter == firstLetter.toLowerCase() )
			className = firstLetter.toUpperCase() + className.substring(1);
		/*
		 * Add the prefix, if necessary.
		 */
		if ( prefix != null )
			className = prefix + className;
		/*
		 * Finally, try to create a new instance.
		 */
		Object out = null;
		try
		{
			out = Class.forName(className).newInstance();
		}
		catch ( Exception e )
		{
			LogFile.shoutLog(
					"ERROR! Problem in XMLable.getNewInstance("+className+")");
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * General constructor from xmlNodes, returns a new instance directly from
	 * an xml node. Overwrite this method in implementing class if the class
	 * needs constructor arguments (they should be stored within the Node).
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		Element E = (Element) xmlNode;
		if(! E.hasAttribute("class"))
			Feedback.out(LogLevel.CRITICAL, "no className devined in: " + 
					E.getTagName());
		else if(! E.hasAttribute("package"))
			return getNewInstance(xmlNode, E.getAttribute("class"));
		return getNewInstance(E.getAttribute("class") , 
				E.getAttribute("package"));
	}
	
	/**
	 * General constructor from xmlNodes, attempts to resolve package from
	 * className
	 */
	public static Object getNewInstance(Node xmlNode, String className)
	{
		return getNewInstance(className, 
				Idynomics.xmlPackageLibrary.get(className));
	}
}