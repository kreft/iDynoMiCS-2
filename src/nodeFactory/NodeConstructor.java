package nodeFactory;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public interface NodeConstructor {
	
	/**
	 * Get the ModelNode object for this NodeConstructor object
	 * @return
	 */
	public ModelNode getNode();
	
	/**
	 * Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object
	 * @param node
	 */
	public void setNode(ModelNode node);
	
	/**
	 * Create a new minimal object of this class and return it
	 * TODO: we may want to merge this with the xmlable interface
	 * @return
	 */
	public NodeConstructor newBlank();
	
	/**
	 * Add a child object that is unable to register itself properly via the
	 * newBlank call.
	 * @param childObject
	 */
	public default void addChildObject(NodeConstructor childObject)
	{
		/* 
		 * the default is do nothing since if possible the object should
		 * register itself
		 */
	}

	/**
	 * return the default XMLtag for the XML node of this object
	 * TODO: we may want to merge this with the xmlable interface
	 * @return
	 */
	public String defaultXmlTag();
	
	
	/**
	 * Return self in xml format
	 * @return
	 */
	public default String getXml()
	{
		return this.getNode().getXML();
	}

}
