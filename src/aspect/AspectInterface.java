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
	public static void loadAspects(AspectInterface aspectInterface, 
			Node xmlNode)
	{
		Element e = (Element) xmlNode;
		@SuppressWarnings("unchecked")
		AspectReg<Object> aspectReg = (AspectReg<Object>) aspectInterface.reg();
		
		NodeList stateNodes = e.getElementsByTagName("aspect");
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element s = (Element) stateNodes.item(j);
			aspectReg.add(s.getAttribute("name"), loadAspectObject(s,"value","type"));
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
		if (! s.hasChildNodes())	// state node with just attributes //
		{
			switch (s.getAttribute(type)) 
			{
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
		else	// state node with attributes and child nodes //
		{
			switch (s.getAttribute(type)) 
			{
				case "body" :
					return Body.getNewInstance(s);
				case "reaction" :
					return Reaction.getNewInstance(XmlHandler.loadUnique(s, "reaction"));
				case "List" :
					List<Object> temp = new LinkedList<Object>();
					items = XmlHandler.getAll(s, "item");
					for ( int i = 0; i < items.getLength(); i++ )
						temp.add((Object) loadAspectObject((Element) items.item(i),value,type));
					return temp;
				case "HashMap" :
					HashMap<Object,Object> hMap = new HashMap<Object,Object>();
					items = XmlHandler.getAll(s, "item");
					for ( int i = 0; i < items.getLength(); i++ )
					{
						hMap.put((Object) loadAspectObject( (Element) 
								items.item(i),"key","keyType"), (Object) loadAspectObject(
								(Element) items.item(i),value,type));
					}
					return hMap;
			}
		}
		Log.out(tier.CRITICAL, "Aspect interface encountered unidentified object type: " + type);
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
	default boolean checkAspect(String aspect)
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
		return (checkAspect(aspect) ?  reg().getValue(this, aspect) : null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double getDouble(String aspect)
	{
		return (checkAspect(aspect) ? (double) reg().getValue(this, aspect) 
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
		return (checkAspect(aspect) ? (String) reg().getValue(this, aspect) 
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
		return (checkAspect(aspect) ? (int) reg().getValue(this, aspect) 
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
		return (checkAspect(aspect) ? (float) reg().getValue(this, aspect) 
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
		return (checkAspect(aspect) ? (boolean) reg().getValue(this, aspect)
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Event getEvent(String aspect)
	{
		return (checkAspect(aspect) ? (Event) reg().getValue(this, aspect) 
				: null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Calculated getCalculated(String aspect)
	{
		return (checkAspect(aspect) ? (Calculated) reg().getValue(this, aspect) 
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
		return (checkAspect(aspect) ? (String[]) reg().getValue(this, aspect) 
				: null);
	}

}
