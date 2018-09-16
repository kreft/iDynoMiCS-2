/**
 * 
 */
package instantiable;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.Helper;

/**
 * \brief static get new instant methods
 * 
 * The Instance class contains a collection of static methods that perform part
 * or all of the following: assess input class from string or xml, fetch package
 * path from referenceLibrary.ClassRef} if only class name is given, perform 
 * instantiate after object creation <b>only for {@link Instance#getNew(Element, 
 * Settable, String...)} method</b> as this is the method that should be used in
 * combination with the {@link instantiable.Instantiable} interface. All other
 * getNew methods do not call the {@link Instantiable#instantiate(Element, 
 * Settable)} method and thus fall outside the Instantiable paradigm. For
 * specific purposes these other methods may still provide useful code
 * simplification, but use with some caution.
 * 
 * For easy reference make sure target classes are all registered in the {@link 
 * referenceLibrary.ClassRef} library. Classes that are not in the class
 * reference library can only be instantiated with their full path as input.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class Instance
{
	/**
	 * \brief Generic instantiation for objects added through GUI or xml, this
	 * should be used in combination with the 
	 * interface
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
	public static Object getNew(Element xmlElem, Settable parent, 
			String... className)
	{
		Object out = null;
		
		/* If no class name is provided continue try to obtain from xml.  */
		if (className == null || className.length == 0)
		{
			if ( ! xmlElem.hasAttribute(XmlRef.classAttribute) )
			{
				Log.out(Tier.CRITICAL, "No className defined in: " +
						xmlElem.getTagName());
				return null;
			}
			else if ( ! xmlElem.hasAttribute(XmlRef.packageAttribute) )
			{
				return getNew(xmlElem, parent,
						xmlElem.getAttribute(XmlRef.classAttribute));
			}
			else
			{
				//FIXME option for if package name is specified
				return getNew(xmlElem, parent,
						xmlElem.getAttribute(XmlRef.classAttribute));
			}
			
		}
		/* If one class name is provided continue instantiating.  */
		else if (className.length == 1)
		{
			/* if also the path is provided instantiate immediately. */
			if ( className[0].contains(".") )
				out = getNew( className[0], null );
			/* if not lookup the path from the package library. */
			else
			{
				out = getNew( className[0], 
						Idynomics.xmlPackageLibrary.get( className[0] ) );
			}
			if ( parent == null && Log.shouldWrite(Tier.BULK) )
				Log.out(Tier.BULK, "Warning initiating without parent");
			else if ( xmlElem == null && Log.shouldWrite(Tier.BULK) )
				Log.out(Tier.BULK, "Warning initiating without xml element");
			((Instantiable) out).instantiate( xmlElem, parent );
			return out;
		} 
		
		/* If multiple options are given let the user choose what class to
		 * instantiate. */
		else
		{
			return getNew( xmlElem, parent, Helper.obtainInput( 
					className, "select class", false ) );	
		}
	}
	
	/**
	 * FIXME @Deprecated only used in unit test, use getNew(className, null) 
	 * instead
	 * 
	 * \brief general method for creating a new instance for an object from the
	 * classRef library.
	 * 
	 * <b>NOTE: method does not call any post instance initiation methods. This
	 * method falls outside the {@link instantiable.Instantiable} paradigm. For
	 * specific purposes this may provide useful code simplification, but use 
	 * with some caution.</b>
	 * 
	 * @param className {@code String} name of the class to be instantiated.
	 * This method will ensure that the first letter is in upper case, but only
	 * the first!
	 * @param prefix {@code String} prefix to <b>className</b>. Typical format:
	 * "packageName.ClassLibrary$".
	 * @return A new instance of the class required.
	 */
	@Deprecated 
	public static Object getNew(String className)
	{
		return getNew(className, Idynomics.xmlPackageLibrary.get(className));
	}

	/**
	 * \brief Internal method for creating a new instance.
	 * 
	 * <b>NOTE: method does not call any post instance initiation methods. This
	 * method falls outside the {@link instantiable.Instantiable} paradigm. For
	 * specific purposes this may provide useful code simplification, but use 
	 * with some caution.</b>
	 * 
	 * @param className {@code String} name of the class to be instantiated.
	 * This method will ensure that the first letter is in upper case, but only
	 * the first!
	 * @param prefix {@code String} prefix to <b>className</b>. Typical format:
	 * "packageName.ClassLibrary$".
	 * @return A new instance of the class required.
	 */
	public static Object getNew(String className, String prefix)
	{
		className = contstructClassName(className, prefix);
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
	
	public static Object getNewThrows(String className, String prefix) throws 
		InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		className = contstructClassName(className, prefix);
		return Class.forName(className).newInstance();
	}
	
	/**
	 * constructs full class name including prefix using provided prefix or
	 * from the xmlPackageLibrary if the prefix is missing.
	 * 
	 * @param className
	 * @param prefix
	 * @return
	 */
	private static String contstructClassName(String className, String prefix)
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
		 * if path is missing
		 */
		if ( prefix == null &! className.contains("."))
			className = Idynomics.xmlPackageLibrary.getFull( className );
		return className;
	}
	
	@SuppressWarnings("unused")
	private static String getClass(Element xml)
	{
		if ( ! xml.hasAttribute(XmlRef.classAttribute) )
			Log.out(Tier.CRITICAL, "No className defined in: "+ xml.getTagName());
		else if ( ! xml.hasAttribute(XmlRef.packageAttribute) )
			return xml.getAttribute(XmlRef.classAttribute);
		else
			return xml.getAttribute(XmlRef.packageAttribute) + "." + 
			xml.getAttribute(XmlRef.classAttribute);
		return null;
	}
}