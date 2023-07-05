package settable;

import dataIO.Log;
import dataIO.Log.Tier;
/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public interface Settable
{
	/**
	 * Get the Module for this Settable object
	 * @return
	 */
	public Module getModule();
	
	/**
	 * \brief Load and interpret the values of the given Module to this 
	 * Settable object.
	 * 
	 * <p>Updates any values that are newly set or modified.</p>
	 * 
	 * @param node
	 */
	public default void setModule(Module node)
	{
		/* By default if the module has no attributes to be set continue and
		 * set the attributes of all child modules */
		for ( Module n : node.getAllChildModules() )
		{
			if( n.settableObject == null)
				n.settableObject = null;
			n.settableObject.setModule(n);
		}

	}
	
	/**
	 * remove the node from the simulation (gui delete object), specifier is
	 * used to identify nested objects for removal
	 * @param specifier
	 */
	public default void removeModule(String specifier)
	{
		/*
		 * By default assume the module cannot be removed, override if the
		 * module can be removed
		 */
		Log.out(Tier.NORMAL, "Module cannot be removed");
	}
		
	/**
	 * @return Default XML tag for the XML node of this object.
	 */
	public String defaultXmlTag();
	
	/**
	 * @return Description of this object's current state, in XML format.
	 */
	public default String getXml()
	{
		return this.getModule().getXML();
	}

	/**
	 * Set the parent Object
	 * @param parent
	 */
	public void setParent(Settable parent);
	
	/**
	 * Get the parent Object
	 * @return
	 */
	public Settable getParent();
}
