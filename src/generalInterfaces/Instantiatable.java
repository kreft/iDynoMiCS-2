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
	public void init(Element xmlElement, NodeConstructor parent);
	
	/**
	 * \brief Generic instantiation for objects added through GUI or xml.
	 * 
	 * By default supply the XML element, the parent and the class name.
	 * 
	 * The method also allows user dependent class definition (provide valid 
	 * options). This is the way to go when dealing with an abstract class (such
	 * as shape) where only child classes can be initiated and thus they have to
	 * be chosen immediately. 
	 * 
	 * <p><b>IMPORTANT:</b> Static methods cannot be overwritten.</p>
	 * @param className
	 * @param xmlElem
	 * @param parent
	 * @return
	 */
	public static Object getNewInstance(Element xmlElem, NodeConstructor parent, 
			String... className)
	{
		Object out = null;
		/* If no class name is provided continue try to obtain from xml.  */
		if (className == null || className.length == 0)
		{
			if ( ! xmlElem.hasAttribute(XmlRef.classAttribute) )
				Log.out(Tier.CRITICAL, "No className defined in: " +
						xmlElem.getTagName());
			else if ( ! xmlElem.hasAttribute(XmlRef.packageAttribute) )
			{
				return getNewInstance(xmlElem, parent,
						xmlElem.getAttribute(XmlRef.classAttribute));
			}
			//FIXME option for if package name is specified
		}
		/* If one class name is provided continue instantiating.  */
		else if (className.length == 1)
		{
			/* if also the path is provided instantiate immediately. */
			if ( className[0].contains(".") )
				out = getNewInstance( className[0], null );
			/* if not lookup the path from the package library. */
			else
				out = getNewInstance( className[0], 
						Idynomics.xmlPackageLibrary.get( className[0] ) );
		} 
		
		/* If multiple options are given let the user choose what class to
		 * instantiate. */
		else
			out = getNewInstance( xmlElem, parent, Helper.obtainInput( 
					className, "select class", false ) );	
		if (parent == null && Log.shouldWrite(Tier.DEBUG))
			Log.out(Tier.DEBUG, "Warning initiating without parent");
		else if (xmlElem == null && Log.shouldWrite(Tier.DEBUG))
			Log.out(Tier.DEBUG, "Warning initiating without xml element");
		( (Instantiatable) out).init( xmlElem, parent );
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
		return getNewInstance((Element) xmlNode, null, (String[]) null);
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