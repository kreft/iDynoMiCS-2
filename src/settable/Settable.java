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
	 * Get the ModelNode object for this NodeConstructor object
	 * @return
	 */
	public Module getModule();
	
	/**
	 * \brief Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object.
	 * 
	 * <p>Updates any values that are newly set or modified.</p>
	 * 
	 * @param node
	 */
	public default void setModule(Module node)
	{
		for ( Module n : node.getAllChildModules() )
			n.settableObject.setModule(n);
	}
	
	/**
	 * remove the node from the simulation (gui delete object), specifier is
	 * used to identify nested objects for removal
	 * @param specifier
	 */
	public default void removeModule(String specifier)
	{
		/*
		 * By default assume the Node cannot be removed
		 */
		Log.out(Tier.NORMAL, "Module cannot be removed");
	}
	
	public default void removeChildModule(Settable childNode)
	{
		/*
		 * By default do nothing, only applicable for Nodes that have childnodes
		 */
		Log.out(Tier.NORMAL, "Module cannot remove child module");
	}
	
	/**
	 * \brief Add a child object that is unable to register itself properly via
	 * the newBlank call.
	 * 
	 * <p>This is likely called by the GUI.</p>
	 * 
	 * @param childObject
	 */
	public default void addChildObject(Settable childObject)
	{
		/* 
		 * The default is do nothing since if possible the object should
		 * register itself.
		 */
	}

	/**
	 * @return Default XML tag for the XML node of this object.
	 */
	//TODO: we may want to merge this with the xmlable interface
	public String defaultXmlTag();
	
	
	/**
	 * @return Description of this object's current state, in XML format.
	 */
	public default String getXml()
	{
		return this.getModule().getXML();
	}

	public void setParent(Settable parent);
	
	public Settable getParent();
}
