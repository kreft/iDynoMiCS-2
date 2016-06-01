package aspect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import dataIO.XmlLabel;

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
	 * @param <A>
	 * 
	 * @return
	 */
	public AspectReg reg();	
	
	/**
	 * \brief Load all aspects from xmlNode into anything that implements the
	 * StateObject interface.
	 * 
	 * @param aspectReg
	 * @param xmlNode
	 */
	public default void loadAspects(Node xmlNode)
	{
		Element e = (Element) xmlNode;
		AspectReg aspectReg = (AspectReg) reg();
		String  name;
		NodeList stateNodes = e.getElementsByTagName(XmlLabel.aspect);
		for (int j = 0; j < stateNodes.getLength(); j++) 
		{
			Element s = (Element) stateNodes.item(j);
			name = s.getAttribute(XmlLabel.nameAttribute);
			aspectReg.add(name, ObjectFactory.loadObject(s));
			Log.out(Tier.BULK, "Aspects loaded for \""+name+"\"");
		}
		
//		reg().getXml();
	}
	
//	/**
//	 * quick method to load simple aspects from user input
//	 * @param name
//	 * @param input
//	 * @param type
//	 */
//	public default void loadAspect(String name, String input, String type)
//	{
//		AspectReg<Object> aspectReg = (AspectReg<Object>) reg();
//		aspectReg.add(name, ObjectFactory.loadObject(input, type));
//		Log.out(Tier.BULK, "Aspects loaded for \""+name+"\"");
//	}
	
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
	
	public default void set(String key, Object aspect)
	{
		reg().set(key, aspect);
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
	// TODO Rob [24May2016]: change this from Double[] to double[]?
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
