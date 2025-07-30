package aspect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dataIO.Log;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.Aspect.AspectClass;
import dataIO.ObjectFactory;
import referenceLibrary.XmlRef;

/**
 * The aspect interface is implemented by classes with an aspect registry,
 * allows for direct interaction with the aspect registry and easy loading of 
 * aspects from xml.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 * TODO: investigate mixed use of primitive and object type such as double and Double
 * advantage of object type might be that it does not directly lead to a crash if it happens to be
 * set to null.
 */
public abstract interface AspectInterface
{
	/**
	 * \brief TODO
	 * @return
	 */
	public AspectReg reg();	
	
	/**
	 * \brief Load all aspects from xmlNode into anything that implements the
	 * StateObject interface.
	 *
	 * @param xmlNode
	 */
	public default void loadAspects(Node xmlNode)
	{
		if (xmlNode != null)
		{
			Element e = (Element) xmlNode;
			AspectReg aspectReg = (AspectReg) reg();
			String  key;
			NodeList stateNodes = e.getElementsByTagName( XmlRef.aspect );
			for (int j = 0; j < stateNodes.getLength(); j++) 
			{
				Element s = (Element) stateNodes.item(j);
				if(s.getParentNode() == e)
				{
					key = s.getAttribute( XmlRef.nameAttribute );
					aspectReg.add( key, ObjectFactory.loadObject( s ) );
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
	 * check for local existence of aspect
	 * @param aspect
	 * @return
	 */
	default boolean isLocalAspect(String aspect)
	{
		return this.reg().isLocalAspect(aspect);
	}
	
	/**
	 * check for global existence of aspect
	 * @param aspect
	 * @return
	 */
	default boolean isAspect(String aspect)
	{
		return this.reg().isGlobalAspect(aspect);
	}
	
	/**
	 * 
	 * @param aspect
	 * @return
	 */
	default AspectClass getType(String aspect)
	{
		return reg().getType(this, aspect);
	}
	
	/**
	 * 
	 * @param key
	 * @param aspect
	 */
	public default void set(String key, Object aspect)
	{
		this.reg().set(key, aspect);
	}

	public default void delete(String key)
	{
		this.reg().remove(key);
	}
	
	/**
	 * get value or use default if the aspect is not set
	 * @param aspect
	 * @param defaultValue
	 * @return
	 */
	public default Object getOr(String aspect, Object defaultValue)
	{
		return ( this.isAspect(aspect) ? 
				this.getValue(aspect) : defaultValue );
	}
	
	/**
	 * Getting raw aspect object
	 * @param aspect
	 * @return
	 */
	public default Object getValue(String aspect)
	{
		return this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double getDouble(String aspect)
	{
		try {
			return (Double) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Double.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default double[] getDoubleA(String aspect)
	{
		try {
		return (double[]) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Double array.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String getString(String aspect)
	{
		try {
		return (String) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to String.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String[] getStringA(String aspect)
	{
		try {
			return (String[]) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to String array.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer getInt(String aspect)
	{
		try {
			return (Integer) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Integer.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer[] getIntA(String aspect)
	{
		try {
			return (Integer[]) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Integer array.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
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
		try {
			return (Float) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Float.");
			Log.out(Log.Tier.DEBUG, e.getStackTrace().toString());
			return null;
		}
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
		try {
			return (Float[]) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Float.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean getBoolean(String aspect)
	{
		Boolean out = null;
		try {
			out = (Boolean) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Boolean.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
        return out;
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean[] getBooleanA(String aspect)
	{
		try {
			return (Boolean[]) this.reg().getValue(this, aspect);
		}
		catch(java.lang.ClassCastException e) {
			Log.out(Log.Tier.CRITICAL, "Cannot cast "+ aspect +" to Boolean array.");
			Log.out(Log.Tier.DEBUG, e.toString());
			return null;
		}
	}

	public default Map<String, Double> getVariables(Collection<String> variables)
	{
		HashMap<String, Double> out = new HashMap<String, Double>();
		for( String s : variables)
			out.put(s, this.getDouble(s));
		return out;
	}
}
