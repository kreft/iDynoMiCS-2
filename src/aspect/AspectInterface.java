package aspect;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Body;
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
			if (! s.hasChildNodes())	// state node with just attributes //
			{
				switch (s.getAttribute("type")) 
				{
					case "boolean" : 
						aspectReg.add(s.getAttribute("name"), 
								Boolean.valueOf(s.getAttribute("value")));
	                	break;
					case "int" : 
						aspectReg.add(s.getAttribute("name"), 
								Integer.valueOf(s.getAttribute("value")));
						break;
					case "int[]" : 
						aspectReg.add(s.getAttribute("name"), 
								Vector.intFromString(s.getAttribute("value")));
	                	break;
					case "double" : 
						aspectReg.add(s.getAttribute("name"), 
								Double.valueOf(s.getAttribute("value")));
						break;
					case "double[]" : 
						aspectReg.add(s.getAttribute("name"), 
								Vector.dblFromString(s.getAttribute("value")));
	                	break;
					case "String" : 
						aspectReg.add(s.getAttribute("name"), 
								s.getAttribute("value"));
	                	break;
					case "String[]" : 
						aspectReg.add(s.getAttribute("name"), 
								s.getAttribute("value").split(","));
	                	break;
					case "calculated" : 
						aspectReg.add(s.getAttribute("name"), 
								Calculated.getNewInstance(s));
	                	break;
					case "event" :
						aspectReg.add(s.getAttribute("name"), 
								Event.getNewInstance(s));
				}
			}
			else	// state node with attributes and child nodes //
			{
				switch (s.getAttribute("type")) 
				{
					case "body" :
						aspectReg.add("body", Body.getNewInstance(s));
						break;
					case "List" :
						List<Object> temp = new LinkedList<Object>();
						NodeList items = XmlHandler.getAll(s, "item");
						for ( int i = 0; i < items.getLength(); i++ )
							temp.add((Object) ((Element) items.item(i)).getAttribute("value"));
						aspectReg.add(s.getAttribute("name"),temp);
						break;
				}
			}
		}
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
