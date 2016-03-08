package aspect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import agent.Body;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import linearAlgebra.Vector;
import reaction.Reaction;

/**
 * The aspect interface is implemented by classes with an aspect registry,
 * allows for direct interaction with the aspect registry and easy loading of 
 * aspects from xml.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract interface AspectInterface
{
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public AspectReg<?> reg();
	
	/**
	 * \brief Load all states from xmlNode into anything that implements the
	 * StateObject interface.
	 * 
	 * @param aspectReg
	 * @param xmlNode
	 */
	public default void loadAspects(Node xmlNode)
	{
		Element e = (Element) xmlNode;
		@SuppressWarnings("unchecked")
		AspectReg<Object> aspectReg = (AspectReg<Object>) reg();
		String  name;
		NodeList stateNodes = e.getElementsByTagName(XmlLabel.aspect);
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element s = (Element) stateNodes.item(j);
			name = s.getAttribute(XmlLabel.nameAttribute);
			aspectReg.add(name, loadAspectObject(s, XmlLabel.valueAttribute,
													XmlLabel.typeAttribute));
			Log.out(Tier.BULK, "Aspects loaded for \""+name+"\"");
		}
	}
	
	/**
	 * quick method to load simple aspects from user input
	 * @param name
	 * @param input
	 * @param type
	 */
	public default void loadAspect(String name, String input, String type)
	{
		@SuppressWarnings("unchecked")
		AspectReg<Object> aspectReg = (AspectReg<Object>) reg();
		aspectReg.add(name, loadAspectObjectFromString(input, type));
		Log.out(Tier.BULK, "Aspects loaded for \""+name+"\"");
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
		Log.out(Tier.CRITICAL, "Aspect interface encountered unidentified "
				+ "object type: " + type);
		return null;
	}
	
	/**
	 * quick method to return simple aspects from user input
	 * @param input
	 * @param type
	 * @return
	 */
	public static Object loadAspectObjectFromString(String input, String type)
	{
		NodeList items;
		switch (type) 
		{
		/* state node with just attributes */
			case "boolean" : 
				return Boolean.valueOf(input);
			case "int" : 
				return Integer.valueOf(input);
			case "int[]" : 
				return Vector.intFromString(input);
			case "double" : 
				return Double.valueOf(input);
			case "double[]" : 
				return Vector.dblFromString(input);
			case "String" : 
				return input;
			case "String[]" : 
				return input.split(",");
			case "calculated" : 
				return Calculated.getNewInstance(input);
			case "event" :
				return Event.getNewInstance(input);
			case "body" :
				return Body.getNewInstance(input);
			case "reaction" :
				return Reaction.getNewInstance(complexAspectLoading(input));
			case "List" :
				List<Object> temp = new LinkedList<Object>();
				items = XmlHandler.getAll(complexAspectLoading(input), 
						XmlLabel.item);
				for ( int i = 0; i < items.getLength(); i++ )
					temp.add((Object) loadAspectObject(
							(Element) items.item(i), XmlLabel.valueAttribute, 
							XmlLabel.typeAttribute));
				return temp;
			case "HashMap" :
				HashMap<Object,Object> hMap = new HashMap<Object,Object>();
				items = XmlHandler.getAll(complexAspectLoading(input), XmlLabel.item);
				for ( int i = 0; i < items.getLength(); i++ )
				{
					hMap.put((Object) loadAspectObject((Element) 
							items.item(i), XmlLabel.keyAttribute ,
							XmlLabel.keyTypeAttribute ), 
							(Object) loadAspectObject((Element) 
							items.item(i), XmlLabel.valueAttribute, XmlLabel.typeAttribute));
				}
				return hMap;
		}
		Log.out(Tier.CRITICAL, "Aspect interface encountered unidentified "
				+ "object type: " + type);
		return null;
	}
	
	/**
	 * enables loading of aspects with complex structure in the gui by allowing
	 * xml formatted input
	 * @param input
	 * @return
	 */
	static Node complexAspectLoading(String input)
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
		if ( reg().isGlobalAspect(aspect) )
		{
			if ( reg().getValue(this, aspect) != null )
				return true;
			else
			{
				Log.out(Tier.BULK, "Aspect \""+aspect+"\" found but null");
				return false;
			}
		}
		else
		{
			Log.out(Tier.BULK, "Aspect \""+aspect+"\" not found");
			return false;
		}
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
}
