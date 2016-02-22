package aspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Body;
import dataIO.Log;
import dataIO.Log.tier;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import linearAlgebra.Vector;
import reaction.Reaction;
import surface.BoundingBox;

/**
 * The aspect interface is implemented by classes with an aspect registry,
 * allows for direct interaction with the aspect registry and easy loading of 
 * aspects from xml.
 * @author baco
 *
 */
public abstract interface AspectInterface {
	
	public AspectReg<?> reg();
	
	/**
	 * Loads all states from xmlNode into anything that implements the
	 * StateObject interface.
	 * @param aspectReg
	 * @param xmlNode
	 */
	public default void loadAspects(Node xmlNode)
	{
		Element e = (Element) xmlNode;
		@SuppressWarnings("unchecked")
		AspectReg<Object> aspectReg = (AspectReg<Object>) reg();
		
		NodeList stateNodes = e.getElementsByTagName(XmlLabel.aspect);
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element s = (Element) stateNodes.item(j);
			aspectReg.add(s.getAttribute(XmlLabel.nameAttribute), 
					loadAspectObject(s,XmlLabel.valueAttribute,
					XmlLabel.typeAttribute));
		}
	}
	
	/**
	 * Identifies appropriate loading method for aspect or item and applies this
	 * method to return a new object of the approriate type
	 * @param s
	 * @return
	 */
	public static Object loadAspectObject(Element s, String value, String type)
	{
		NodeList items;
		if (! s.hasChildNodes())	
		{
			switch (s.getAttribute(type)) 
			{
			/* state node with just attributes */
				case "boolean" : 
					return Boolean.valueOf(s.getAttribute(value));
				case "int" : 
					return Integer.valueOf(s.getAttribute(value));
				case "int[]" : 
					return Vector.intFromString(s.getAttribute(value));
				case "double" : 
					return Double.valueOf(s.getAttribute(value));
				case "double[]" : 
					return Vector.dblFromString(s.getAttribute(value));
				case "String" : 
					return s.getAttribute(value);
				case "String[]" : 
					return s.getAttribute(value).split(",");
				case "calculated" : 
					return Calculated.getNewInstance(s);
				case "event" :
					return Event.getNewInstance(s);
			}
		}
		else	
		{
			/* state node with attributes and child nodes */
			switch (s.getAttribute(type)) 
			{
				case "body" :
					return Body.getNewInstance(s);
				case "reaction" :
					return Reaction.getNewInstance( XmlHandler.loadUnique(s, 
							"reaction"));
				case "List" :
					List<Object> temp = new LinkedList<Object>();
					items = XmlHandler.getAll(s, XmlLabel.item);
					for ( int i = 0; i < items.getLength(); i++ )
						temp.add((Object) loadAspectObject(
								(Element) items.item(i), value, type));
					return temp;
				case "HashMap" :
					HashMap<Object,Object> hMap = new HashMap<Object,Object>();
					items = XmlHandler.getAll(s, XmlLabel.item);
					for ( int i = 0; i < items.getLength(); i++ )
					{
						hMap.put((Object) loadAspectObject((Element) 
								items.item(i), XmlLabel.keyAttribute ,
								XmlLabel.keyTypeAttribute ), 
								(Object) loadAspectObject((Element) 
								items.item(i), value, type ));
					}
					return hMap;
			}
		}
		Log.out(tier.CRITICAL, "Aspect interface encountered unidentified "
				+ "object type: " + type);
		return null;
	}
	
	/**************************************************************************
	 * Quick getter methods, making life easy and code readable, expand as new
	 * objects are implemented in the aspect interface
	 * NOTE: there may be more efficient ways of doing this, check
	 */
	
	/**
	 * check for global existence of aspect
	 * @param aspect
	 * @return
	 */
	default boolean isAspect(String aspect)
	{
		return reg().isGlobalAspect(aspect) && reg().getValue(this, aspect) 
				!= null;
	}
	
	/**
	 * Getting raw aspect object
	 * @param aspect
	 * @return
	 */
	public default Object getValue(String aspect)
	{
		return (isAspect(aspect) ?  reg().getValue(this, aspect) : null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double getDouble(String aspect)
	{
		return (isAspect(aspect) ? (double) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double[] getDoubleA(String aspect)
	{
		return (isAspect(aspect) ? (Double[]) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String getString(String aspect)
	{
		return (isAspect(aspect) ? (String) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String[] getStringA(String aspect)
	{
		return (isAspect(aspect) ? (String[]) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer getInt(String aspect)
	{
		return (isAspect(aspect) ? (int) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer[] getIntA(String aspect)
	{
		return (isAspect(aspect) ? (Integer[]) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null.
	 * NOTE: floats are not used within iDynoMiCS, yet available to combine with
	 * external packages / models that require floats.
	 * @param aspect
	 * @return
	 */
	public default Float getFloat(String aspect)
	{
		return (isAspect(aspect) ? (float) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null.
	 * NOTE: floats are not used within iDynoMiCS, yet available to combine with
	 * external packages / models that require floats.
	 * @param aspect
	 * @return
	 */
	public default Float[] getFloatA(String aspect)
	{
		return (isAspect(aspect) ? (Float[]) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean getBoolean(String aspect)
	{
		return (isAspect(aspect) ? (boolean) reg().getValue(this, aspect)
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean[] getBooleanA(String aspect)
	{
		return (isAspect(aspect) ? (Boolean[]) reg().getValue(this, aspect)
				: null);
	}
	
	
// this is the aspect registries own bussiness
//	/**
//	 * check, cast and return the event object, return null if the aspect does 
//	 * not exist or is equal to null
//	 * @param aspect
//	 * @return
//	 */
//	public default Event getEvent(String aspect)
//	{
//		return (isAspect(aspect) ? (Event) reg().getValue(this, aspect) 
//				: null);
//	}
//	
//	/**
//	 * check, cast and return the calculated state object, return null if the 
//	 * aspect does not exist or is equal to null
//	 * @param aspect
//	 * @return
//	 */
//	public default Calculated getCalculated(String aspect)
//	{
//		return (isAspect(aspect) ? (Calculated) reg().getValue(this, aspect) 
//				: null);
//	}
}
