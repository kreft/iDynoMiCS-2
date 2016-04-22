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
	 * @return
	 */
	public NodeConstructor newBlank();
	
	/**
	 * Add a child object
	 * @param childObject
	 */
	public void addChildObject(NodeConstructor childObject);

	/**
	 * return the default XMLtag for the XML node of this object
	 * @return
	 */
	public String defaultXmlTag();

}
