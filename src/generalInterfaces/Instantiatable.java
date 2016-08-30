/**
 * 
 */
package generalInterfaces;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import nodeFactory.NodeConstructor;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * \brief Implementations of this interface will be able to instanciate and
 * initialise from a XML protocol file.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public interface Instantiatable
{
	/*************************************************************************
	 * CLASS INSTANCIATION
	 * @param parent 
	 ************************************************************************/
	/**
	 * \brief method for Instantiatable object initiation
	 * 
	 * Overwrite this method in the implementing class
	 * 
	 * @param xmlElement
	 * @param parent
	 */
	public default void init(Element xmlElement, NodeConstructor parent)
	{
		// by default nothing
		Log.out(Tier.CRITICAL, "Warning: Instantiable Object class has no implementation"
				+ "of init(Element, NodeConstructor");
	}
	
	/**
	 * \brief External method for creating a new instance.
	 * 
	 * <p>Remember to typecast when using this. E.g.,</p>
	 * <p>{@code this.thing = (Thing) Thing.getNewInstance(className);}.</p>
	 * 
	 * <p><b>IMPORTANT:</b> Static methods cannot be overwritten</p>
	 * 
	 * @param className {@code String} name of the class to be instanciated.
	 * This method will ensure that the first letter is in upper case, but only
	 * the first!
	 * @return A new instance of the class required.
	 */
	public static Object getNewInstance(String className)
	{
		return getNewInstance(className, Idynomics.xmlPackageLibrary.get(className));
	}
	
	/**
	 * \brief Generic instantiation for objects added through GUI or xml.
	 * 
	 * <p><b>IMPORTANT:</b> Static methods cannot be overwritten.</p>
	 * @param className
	 * @param xmlElem
	 * @param parent
	 * @return
	 */
	public static Object getNewInstance(String className, Element xmlElem, NodeConstructor parent)
	{
		Object out;
		if (className.contains("."))
			out = getNewInstance(className, null);
		else
			out = getNewInstance(className, Idynomics.xmlPackageLibrary.get(className));
		((Instantiatable) out).init(xmlElem, parent);
		return out;
	}

	/**
	 * \brief Internal method for creating a new instance.
	 * 
	 * <p><b>IMPORTANT:</b> This method should only be called by the class that
	 * implements Instantiatable.</p>
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
		 * Check the first letter is upper case if a separate prefix is provided.
		 */
		if ( prefix != null )
			className = Helper.firstToUpper(className);
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
			Log.out(Tier.CRITICAL,
					"ERROR! Problem in XMLable.getNewInstance("+className+")");
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * \brief General constructor from xmlNodes, returns a new instance
	 * directly from an XML node.
	 * 
	 * <p><b>IMPORTANT:</b> Static methods cannot be overwritten.</p>
	 * 
	 * @param xmlNode Input from protocol file.
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		Element E = (Element) xmlNode;
		if ( ! E.hasAttribute(XmlRef.classAttribute) )
			Log.out(Tier.CRITICAL, "No className defined in: "+E.getTagName());
		else if ( ! E.hasAttribute(XmlRef.packageAttribute) )
		{
			return getNewInstance(xmlNode, 
									E.getAttribute(XmlRef.classAttribute));
		}
		return getNewInstance(E.getAttribute(XmlRef.classAttribute) , 
									E.getAttribute(XmlRef.packageAttribute));
	}
	
	/**
	 * \brief General constructor from xmlNodes, attempts to resolve package
	 * from <b>className</b>.
	 * 
	 * @param xmlNode Input from protocol file.
	 * @param className 
	 */
	public static Object getNewInstance(Node xmlNode, String className)
	{
		return getNewInstance(className, 
								Idynomics.xmlPackageLibrary.get(className));
	}
}