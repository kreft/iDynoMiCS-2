package dataIO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import agent.Body;
import aspect.Calculated;
import aspect.Event;
import dataIO.Log.Tier;
import generalInterfaces.Copyable;
import generalInterfaces.XMLable;
import linearAlgebra.Array;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import reaction.Reaction;
import utility.Helper;

/**
 * \brief General object loading from XML or string.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ObjectFactory
{
	///////////////////////////////////
	// Object loading
	///////////////////////////////////

	/**
	 * Identifies appropriate loading method for aspect or item and applies this
	 * method to return a new object of the appropriate type, uses String as
	 * input
	 * @param input
	 * @param type
	 * @return
	 */
	public static Object loadObject(String input, String type)
	{
		switch ( type ) 
		{
		/* state node with just attributes */
			case ObjectRef.BOOL : 
				return Boolean.valueOf(input);
			case ObjectRef.INT : 
				try{
					return Integer.valueOf(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.INT);
					return null;
				}
			case ObjectRef.INT_VECT : 
				try{
					return Vector.intFromString(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.INT_VECT);
					return null;
				}
			case ObjectRef.INT_MATR :
				try{
					return Matrix.intFromString(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.INT_MATR);
					return null;
				}
			case ObjectRef.INT_ARRY :
				try{
					return Array.intFromString(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.INT_ARRY);
					return null;
				}
			case ObjectRef.DBL : 
				try{
					return Double.valueOf(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.DBL);
					return null;
				}
			case ObjectRef.DBL_VECT : 
				try{
					return Vector.dblFromString(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.DBL_VECT);
					return null;
				}
			case ObjectRef.DBL_MATR :
				try{
					return Matrix.dblFromString(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.DBL_MATR);
					return null;
				}
			case ObjectRef.DBL_ARRY :
				try{
					return Array.dblFromString(input);
				}
				catch(NumberFormatException e)
				{
					printReadError(input, ObjectRef.DBL_ARRY);
					return null;
				}
			case ObjectRef.STR : 
				return input;
			case ObjectRef.STR_VECT : 
				return input.split(",");
			case "CALCULATED" : 
				return Calculated.getNewInstance(input);
			case "EVENT" :
				return Event.getNewInstance(input);
			case "Body" :
				return Body.getNewInstance(input);
			case "Reaction" :
				return Reaction.getNewInstance(ObjectFactory.stringToNode(input));
			case "LinkedList" :
				return ObjectFactory.xmlList(input);
			case "HashMap" :
				return ObjectFactory.xmlHashMap(input);
		}
		Log.out(Tier.CRITICAL, "Object factory encountered unidentified "
				+ "object type: " + type);
		return null;
	}

	/**
	 * Identifies appropriate loading method for aspect or item and applies this
	 * method to return a new object of the appropriate type. Uses Element as
	 * input
	 * @param s
	 * @return
	 */
	public static Object loadObject(Element s, String value, String type)
	{
		String sType = s.getAttribute(type);
		switch ( sType )
		{
		/* state node with just attributes */
			case ObjectRef.BOOL : 
				return Boolean.valueOf(s.getAttribute(value));
			case ObjectRef.INT : 
				try{
					return Integer.valueOf(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.INT);
					return null;
				}
			case ObjectRef.INT_VECT : 
				try{
					return Vector.intFromString(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.INT_VECT);
					return null;
				}
			case ObjectRef.INT_MATR :
				try{
					return Matrix.intFromString(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.INT_MATR);
					return null;
				}
			case ObjectRef.INT_ARRY :
				try{
					return Array.intFromString(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.INT_ARRY);
					return null;
				}
			case ObjectRef.DBL : 
				try{
					return Double.valueOf(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.DBL);
					return null;
				}
			case ObjectRef.DBL_VECT : 
				try{
					return Vector.dblFromString(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.DBL_VECT);
					return null;
				}
			case ObjectRef.DBL_MATR :
				try{
					return Matrix.dblFromString(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.DBL_MATR);
					return null;
				}
			case ObjectRef.DBL_ARRY :
				try{
					return Array.dblFromString(s.getAttribute(value));
				}
				catch(NumberFormatException e)
				{
					printReadError(s.getAttribute(value), ObjectRef.DBL_ARRY);
					return null;
				}
			case "String" : 
				return s.getAttribute(value);
			case "String[]" : 
				return s.getAttribute(value).split(",");
			case "CALCULATED" : 
				return Calculated.getNewInstance(s);
			case "EVENT" :
				return Event.getNewInstance(s);
			case "Body" :
				return Body.getNewInstance(s);
			case "Reaction" :
				return Reaction.getNewInstance(s);
			case "LinkedList" :
				return ObjectFactory.xmlList(s);
			case "HashMap" :
				return ObjectFactory.xmlHashMap(s);
		}
		Log.out(Tier.CRITICAL, "Object factory encountered unidentified "
				+ "object type: " + sType);
		return null;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param input
	 * @param type
	 */
	private static void printReadError(String input, String type)
	{
		Log.out(Tier.CRITICAL, "Error could not load input as "
				+type+": "+input);
	}
	
	/**
	 * load standard aspect object (use labeling as defined by XmlLabel class).
	 * @param s
	 * @return
	 */
	public static Object loadObject(Element s)
	{
		return loadObject(s, XmlLabel.valueAttribute, 
				XmlLabel.typeAttribute);
	}

	/**
	 * Helper method that converts string to xml node for complex objects
	 * xml formatted input
	 * @param input
	 * @return
	 */
	public static Node stringToNode(String input)
	{
		Node node = null;
		try {
			node = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				    .parse(new ByteArrayInputStream(input.getBytes()))
				    .getDocumentElement();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}
	
	/**
	 * Construct a LinkedList from an xml element
	 * @param s
	 * @return
	 */
	public static LinkedList<?> xmlList(Element s)
	{
		NodeList items;
		LinkedList<Object> temp = new LinkedList<Object>();
		items = XmlHandler.getAll(s, XmlLabel.item);
		for ( int i = 0; i < items.getLength(); i++ )
			temp.add((Object) loadObject((Element) items.item(i), 
					XmlLabel.valueAttribute, XmlLabel.typeAttribute));
		return temp;
	}

	/**
	 * construct a LinkedList from a String formated xml element
	 * @param s
	 * @return
	 */
	public static LinkedList<?> xmlList(String s)
	{
		NodeList items;
		LinkedList<Object> temp = new LinkedList<Object>();
		items = XmlHandler.getAll(ObjectFactory.stringToNode(s), 
				XmlLabel.item);
		for ( int i = 0; i < items.getLength(); i++ )
			temp.add((Object) loadObject((Element) items.item(i), 
					XmlLabel.valueAttribute, XmlLabel.typeAttribute));
		return temp;
	}

	/**
	 * construct a HashMap from a xml element
	 * @param s
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(Element s)
	{
		NodeList items;
		HashMap<Object,Object> hMap = new HashMap<Object,Object>();
		items = XmlHandler.getAll(s, XmlLabel.item);
		for ( int i = 0; i < items.getLength(); i++ )
		{
			hMap.put((Object) loadObject((Element) items.item(i), 
					XmlLabel.keyAttribute , XmlLabel.keyTypeAttribute ), 
					(Object) loadObject((Element) items.item(i), 
					XmlLabel.valueAttribute, XmlLabel.typeAttribute ));
		}
		return hMap;
	}

	/**
	 * construct a HahMap from a String formated xml element
	 * @param s
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(String s)
	{
		NodeList items;
		HashMap<Object,Object> hMap = new HashMap<Object,Object>();
		items = XmlHandler.getAll(ObjectFactory.stringToNode(s), XmlLabel.item);
		for ( int i = 0; i < items.getLength(); i++ )
		{
			hMap.put((Object) loadObject((Element) items.item(i), 
					XmlLabel.keyAttribute , XmlLabel.keyTypeAttribute ), 
					(Object) loadObject((Element) items.item(i), 
					XmlLabel.valueAttribute, XmlLabel.typeAttribute ));
		}
		return hMap;
	}
	
	///////////////////////////////////
	// Xml writing
	///////////////////////////////////
    
    /**
     * TODO work in progress
     * return partial xml specification of the input object, XMLables are
     * included as child node, simple objects are include in the value
     * attribute.
     * @param obj
     * @param typeLabel
     * @param valLabel
     * @return
     */
    public static String specString(Object obj, String typeLabel, 
    		String valLabel)
    {
    	String simpleName = obj.getClass().getSimpleName();
    	String out = "";
    	if (obj instanceof XMLable)
		{
			XMLable x = (XMLable) obj;
			out = out + " " + typeLabel + "=\"" + simpleName + "\">\n" + 
			x.getXml();
		}
		else
		{
	    	switch (simpleName)
    		{
    		case "String[]":
    			out = out + " " + typeLabel + "=\"" + simpleName+ "\" " + 
    					valLabel + "=\"" + Helper.StringAToString(
    					(String[]) obj) + "\"";
    			break;
    		default:
    			out = out + " " + typeLabel + "=\"" + simpleName+ "\" " + 
    					valLabel + "=\"" + obj.toString() + "\"";
    		}
		}
    	return out;
    }
    
    /**
     * Return a node with xmlTag as tag from input object, name attribute is
     * optional.
     * @param obj
     * @param xmlTag
     * @param nameAttribute
     * @return
     */
    public static String nodeFactory(Object obj, String xmlTag, 
    		String nameAttribute)
    {
    	String out = "<" + xmlTag + " " + (nameAttribute != null ? 
    			XmlLabel.nameAttribute + "=\"" + nameAttribute + "\"" : ""); 	
    	String simpleName = obj.getClass().getSimpleName();
    	switch (simpleName)
		{
		case "HashMap":
			@SuppressWarnings("unchecked")
			HashMap<Object,Object> h = (HashMap<Object,Object>) obj;
			out = out + " " + XmlLabel.typeAttribute + "=\"" + simpleName + 
					"\">\n";
			for(Object hKey : h.keySet())
			{
				out = out + "<" + XmlLabel.item + " " + 
						specString( 
								hKey, 
								XmlLabel.keyTypeAttribute, 
								XmlLabel.keyAttribute) +
						specString(
								h, 
								XmlLabel.typeAttribute, 
								XmlLabel.valueAttribute)
						+ (h instanceof XMLable ? "</" + XmlLabel.item + ">\n" :
								"/>\n");

			}
			out = out + "</" + xmlTag + ">\n";
			break;
		case "LinkedList":
			@SuppressWarnings("unchecked")
			LinkedList<Object> l = (LinkedList<Object>) obj;
			out = out + " type=\"" + simpleName + "\">\n";
			for(Object o : l)
			{
				out = out + "<" + XmlLabel.item + " " +
						specString(o, XmlLabel.typeAttribute,
						XmlLabel.valueAttribute) + (o instanceof XMLable ? 
						"</" + XmlLabel.item + ">\n" : "/>\n");

			}
			out = out + "</" + xmlTag + ">\n";
			break;
		default:
			out = out + specString(obj, 
					XmlLabel.typeAttribute, XmlLabel.valueAttribute)
					+ (obj instanceof XMLable ? "</" + XmlLabel.item + ">\n"
					: "/>\n");
		}
		
    	return out;
    }

	/**
	 * Attempts to create a deep copy of any input object
	 * @param <T>
	 * @param <K>
	 * @param copyable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, K> Object copy(Object copyable)
	{
		if (copyable == null)
		{
			Log.out(Tier.DEBUG, "Copier returns a null object");
			return null;
		}
		if (copyable instanceof Double || copyable.getClass() == double.class)
		{
			return new Double((double) copyable);
		}
		if (copyable instanceof Double[] || copyable.getClass() == 
				double[].class)
		{
			return Vector.copy((double[]) copyable);
		}
		if (copyable instanceof Integer || copyable.getClass() == int.class)
		{
			return new Integer((int) copyable);
		}
		if (copyable instanceof Integer[] || copyable.getClass() == 
				int[].class)
		{
			return Vector.copy((int[]) copyable);
		}
		if (copyable instanceof Boolean || copyable.getClass() == boolean.class)
		{
			return new Boolean((boolean) copyable);
		}
		if (copyable instanceof Boolean[] || copyable.getClass() == 
				boolean[].class)
		{
			return Vector.copy((boolean[]) copyable);
		}
		if  (copyable instanceof String)
		{
			// Strings are immutable
			return String.valueOf((String) copyable);
		}
		if (copyable instanceof List<?>)
		{
			List<T> spawn = new LinkedList<T>();
			for(int i = 0; i < ((List<?>) copyable).size(); i++)
				spawn.add((T) ObjectFactory.copy(((List<?>) copyable).get(i)));	
			return spawn;
		}
		if (copyable instanceof HashMap<?,?>)
		{
			Map<K,T> spawn = new HashMap<K,T>();
			for(Object key : ((Map<?,?>) copyable).keySet())
				spawn.put((K) key, (T) ObjectFactory.copy(((Map<?,?>) copyable).get((K) key)));	
			return spawn;
		}
		if (copyable instanceof Copyable)
		{
			return ((Copyable) copyable).copy();
		} 
		else 
		{
			Log.out(Tier.DEBUG,"WARNING: Attempting to deep copy unkown object"
					+ "of type" + copyable.getClass().getName() + " causion!");
			try {
				T duplicate = (T) copyable.getClass().newInstance();
				Field[] fields = duplicate.getClass().getDeclaredFields();
				for(Field f : fields)
				{
					f.set(duplicate, copy(f.get(copyable)));
				}
				return duplicate;				
			} catch (InstantiationException e) {
				Log.out(Tier.CRITICAL, "failed to create new instance of " + 
						copyable.getClass().getName());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "Copier could not acces object of type: "
						+ copyable.getClass().getName());
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}
