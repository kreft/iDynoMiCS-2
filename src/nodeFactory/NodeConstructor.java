package nodeFactory;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public interface NodeConstructor
{
	/**
	 * Get the ModelNode object for this NodeConstructor object
	 * @return
	 */
	public ModelNode getNode();
	
	/**
	 * \brief Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object.
	 * 
	 * <p>Updates any values that are newly set or modified.</p>
	 * 
	 * @param node
	 */
	public default void setNode(ModelNode node)
	{
		for ( ModelNode n : node.getAllChildNodes() )
			n.constructor.setNode(n);
	}
	
	/**
	 * Create a new minimal object of this class and return it
	 * 
	 * @return
	 */
	//TODO: we may want to merge this with the xmlable interface
	@Deprecated
	public default NodeConstructor newBlank()
	{
		return null;
	}
	
	/**
	 * remove the node from the simulation (gui delete object), specifier is
	 * used to identify nested objects for removal
	 * @param specifier
	 */
	public default void removeNode(String specifier)
	{
		/*
		 * By default assume the Node cannot be removed
		 */
	}
	
	/**
	 * \brief Add a child object that is unable to register itself properly via
	 * the newBlank call.
	 * 
	 * <p>This is likely called by the GUI.</p>
	 * 
	 * @param childObject
	 */
	public default void addChildObject(NodeConstructor childObject)
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
		return this.getNode().getXML();
	}
}
