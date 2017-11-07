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
import dataIO.Log.Tier;
import generalInterfaces.Copyable;
import idynomics.Idynomics;
import instantiable.Instance;
import linearAlgebra.Array;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.ClassRef;
import referenceLibrary.ObjectRef;
import referenceLibrary.XmlRef;
import settable.Settable;
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
	 * @param objectClass
	 * @return
	 */
	public static Object loadObject(String input, String objectClass )
	{
		return loadObject( null, input, objectClass, null, null);
	}

	/**
	 * Identifies appropriate loading method for aspect or item and applies this
	 * method to return a new object of the appropriate type. Uses Element as
	 * input
	 * @param s
	 * @return
	 */
	public static Object loadObject( Element s, String value, 
			String objectClassLabel)
	{
		return loadObject( s, value, null, objectClassLabel, null);
	}
	
	private static Object loadObject( Element elem, String input, 
			String objectClass, String objectClassLabel, Settable parent )
	{
		/* Obtain object class */
		if ( Helper.isNullOrEmpty( objectClass ) )
		{
			if ( Helper.isNullOrEmpty( objectClassLabel ) && 
					Log.shouldWrite( Tier.NORMAL ) )
				Log.out(Tier.NORMAL, ObjectFactory.class.getSimpleName() + 
						" could not find any object class specification.");
			else
				objectClass = elem.getAttribute( objectClassLabel );
		}
		if ( Helper.isNullOrEmpty( objectClass ) )
				objectClass = objectClassLabel;
		
		/* Make sure the class is capitalized */
		objectClass = Helper.firstToUpper( objectClass );

		/* identify and create object */
		switch ( objectClass )
		{
		/* state node with just attributes */
		case ObjectRef.BOOL : 
			return Boolean.valueOf( input(input, elem) );
		case ObjectRef.INT : 
			try{
				return Integer.valueOf( input(input, elem) );
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.INT);
				return null;
			}
		case ObjectRef.INT_VECT : 
			try{
				return Vector.intFromString(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.INT_VECT);
				return null;
			}
		case ObjectRef.INT_MATR :
			try{
				return Matrix.intFromString(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.INT_MATR);
				return null;
			}
		case ObjectRef.INT_ARRY :
			try{
				return Array.intFromString(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.INT_ARRY);
				return null;
			}
		case ObjectRef.DBL : 
			try{
				return Double.valueOf(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.DBL);
				return null;
			}
		case ObjectRef.DBL_VECT : 
			try{
				return Vector.dblFromString(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.DBL_VECT);
				return null;
			}
		case ObjectRef.DBL_MATR :
			try{
				return Matrix.dblFromString(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.DBL_MATR);
				return null;
			}
		case ObjectRef.DBL_ARRY :
			try{
				return Array.dblFromString(input(input, elem));
			}
			catch(NumberFormatException e)
			{
				printReadError(input(input, elem), ObjectRef.DBL_ARRY);
				return null;
			}
		case ObjectRef.STR : 
			return input(input, elem);
		case ObjectRef.STR_VECT : 
			return input(input, elem).split(",");
		case ObjectRef.INSTANTIABLELIST :
			return Instance.getNew(elem, null, ClassRef.instantiableList);
		case ObjectRef.INSTANTIABLEMAP :
			return Instance.getNew(elem, null, ClassRef.instantiableMap);
		case ObjectRef.LINKEDLIST :
			return ObjectFactory.xmlList(elem);
		case ObjectRef.HASHMAP :
			return ObjectFactory.xmlHashMap(elem);
		case ObjectRef.REACTION :
			return Instance.getNew(elem,null,ClassRef.reaction);
		case ObjectRef.BODY :
			if ( elem == null )
				Body.instanceFromString( input(input, elem) );
			else
				return Instance.getNew(elem,null,ClassRef.body);
		default:
			if ( Idynomics.xmlPackageLibrary.has( objectClass ))
			{
				Log.out(Tier.CRITICAL, "Oject factory encountered non PRIMARY"
						+ " object " + objectClass);
				return Instance.getNew(elem, null, 
						Idynomics.xmlPackageLibrary.getFull( objectClass ) );
			}
			else
			{
				Log.out(Tier.CRITICAL, "Object factory encountered unidentified"
						+ " object class: " + objectClass);
				return null;
			}
		}
	}
	
	
	private static String input(String input, Element elem)
	{
		/* If we are not using xml and we are missing string input */
		if ( elem == null && input == null)
			input = Helper.obtainInput( "", ObjectFactory.class.getSimpleName()
					+ " requires value." );
		
		/* If we are using xml we obtain the value from xml */
		else if( elem != null && input != null )
			input = elem.getAttribute(input);
		/* Optional exeption for elem != null iput == null */

		/* Either the input was already given or now obtained, return it */
		return input;
	}

	/**
	 * load standard aspect object (use labeling as defined by XmlLabel class).
	 * @param s
	 * @return
	 */
	public static Object loadObject(Element s)
	{
		return loadObject(s, XmlRef.valueAttribute, XmlRef.classAttribute);
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
		items = XmlHandler.getAll(s, XmlRef.item);
		for ( int i = 0; i < items.getLength(); i++ )
			temp.add((Object) loadObject((Element) items.item(i)));
		return temp;
	}

	/**
	 * construct a LinkedList from a String formated xml element
	 * @param s
	 * @return
	 */
	public static LinkedList<?> xmlList(String s)
	{
		return xmlList((Element) ObjectFactory.stringToNode(s));
	}

	/**
	 * construct a HashMap from a xml element
	 * @param s
	 * @return
	 */
	public static HashMap<?,?> xmlHashMap(Element s)
	{
		return xmlHashMap(s, XmlRef.item);
	}
	
	public static HashMap<?,?> xmlHashMap(Element s, String itemNodeLabel)
	{
		NodeList items;
		HashMap<Object,Object> hMap = new HashMap<Object,Object>();
		items = XmlHandler.getAll(s, itemNodeLabel);
		for ( int i = 0; i < items.getLength(); i++ )
		{
			hMap.put((Object) loadObject((Element) items.item(i), 
					XmlRef.keyAttribute , XmlRef.keyClassAttribute ), 
					(Object) loadObject((Element) items.item(i), 
							XmlRef.valueAttribute, XmlRef.classAttribute ));
		}
		return hMap;
	}
	
	public static HashMap<?,?> xmlHashMap(Element s, String itemNodeLabel, 
			String keyClass, String keyAttribute, String valueClass,
			String valueAttribute)
	{
		NodeList items;
		HashMap<Object,Object> hMap = new HashMap<Object,Object>();
		items = XmlHandler.getAll(s, itemNodeLabel);
		for ( int i = 0; i < items.getLength(); i++ )
		{
			hMap.put( (Object) loadObject( (Element) items.item(i),  keyAttribute , 
					keyClass ), 
					(Object) loadObject( (Element) items.item(i), valueAttribute , 
					valueClass ) );
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
		return xmlHashMap( (Element) ObjectFactory.stringToNode(s) );
	}

	//	///////////////////////////////////
	//	// Xml writing
	//	///////////////////////////////////

	/**
	 * TODO work in progress
	 * return partial xml specification of the input object, XMLables are
	 * included as child node, simple objects are include in the value
	 * attribute.
	 * @param obj
	 * @param classLabel
	 * @param valLabel
	 * @return
	 */
	public static String stringRepresentation(Object obj)
	{
		String simpleName = Helper.firstToUpper(obj.getClass().getSimpleName());
		String out = "";
		switch (simpleName)
		{
		case ObjectRef.STR_VECT:
			out = Helper.stringAToString( (String[]) obj );
			break;
		case ObjectRef.DBL_ARRY:
			out = Array.toString( (double[][][]) obj );
			break;
		case ObjectRef.DBL_MATR:
			out = Matrix.toString( (double[][]) obj );
			break;
		case ObjectRef.DBL_VECT:
			out = Vector.toString( (double[]) obj );
			break;
		case ObjectRef.INT_ARRY:
			out = Array.toString( (int[][][]) obj );
			break;
		case ObjectRef.INT_MATR:
			out = Matrix.toString( (int[][]) obj );
			break;
		case ObjectRef.INT_VECT:
			out = Vector.toString( (int[]) obj );
			break;
		default:
			out =  obj.toString();
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
		if  (copyable instanceof String[])
		{
			// Strings are immutable
			return Helper.copyStringA((String[]) copyable);
		}
		if (copyable instanceof Copyable)
		{
			return ((Copyable) copyable).copy();
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
