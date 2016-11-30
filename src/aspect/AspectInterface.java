package aspect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aspect.Aspect.AspectClass;
import dataIO.Log;
import dataIO.ObjectFactory;
import generalInterfaces.Instantiatable;
import dataIO.Log.Tier;
import referenceLibrary.XmlRef;
import utility.Helper;

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
		if (xmlNode != null)
		{
			Element e = (Element) xmlNode;
			AspectReg aspectReg = (AspectReg) reg();
			String  key;
			NodeList stateNodes = e.getElementsByTagName( XmlRef.aspect );
			for (int j = 0; j < stateNodes.getLength(); j++) 
			{
				Element s = (Element) stateNodes.item(j);
				key = s.getAttribute( XmlRef.nameAttribute );
				switch (AspectClass.valueOf( Helper.setIfNone(
						s.getAttribute( XmlRef.typeAttribute ), 
						String.valueOf( AspectClass.PRIMARY ) ) ) )
		    	{
		    	case CALCULATED:
		    		aspectReg.add( key , Instantiatable.getNewInstance( s ) );
		    		break;
		    	case EVENT: 
		    		aspectReg.add( key , Instantiatable.getNewInstance( s ) );
		    		break;
		    	case PRIMARY:
				default:
					aspectReg.add( key, ObjectFactory.loadObject( s ) );
				}
				Log.out(Tier.BULK, "Aspects loaded for \""+key+"\"");
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
	default boolean isAspect(String aspect)
	{
		if ( this.reg().isGlobalAspect(aspect) )
		{
			if ( this.reg().getValue(this, aspect) != null )
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
	 * 
	 * @param key
	 * @param aspect
	 */
	public default void set(String key, Object aspect)
	{
		this.reg().set(key, aspect);
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
		return ( this.isAspect(aspect) ? 
				this.reg().getValue(this, aspect) : null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double getDouble(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(double) this.reg().getValue(this, aspect) : null );
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
		return ( this.isAspect(aspect) ? 
				(Double[]) this.reg().getValue(this, aspect) : null );
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String getString(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(String) this.reg().getValue(this, aspect) : null );
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String[] getStringA(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(String[]) this.reg().getValue(this, aspect) : null );
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer getInt(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(int) this.reg().getValue(this, aspect) : null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer[] getIntA(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(Integer[]) this.reg().getValue(this, aspect) : null );
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
		return ( this.isAspect(aspect) ? 
				(float) this.reg().getValue(this, aspect) : null );
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
		return ( this.isAspect(aspect) ? 
				(Float[]) this.reg().getValue(this, aspect) : null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean getBoolean(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(boolean) this.reg().getValue(this, aspect) : null);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean[] getBooleanA(String aspect)
	{
		return ( this.isAspect(aspect) ? 
				(Boolean[]) this.reg().getValue(this, aspect) : null);
	}

}
