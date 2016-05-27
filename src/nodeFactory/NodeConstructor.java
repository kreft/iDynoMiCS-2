package nodeFactory;

public interface NodeConstructor {
	
	public ModelNode getNode();
	
	public void setNode(ModelNode node);
	
	public NodeConstructor newBlank();
	
	public void addChildObject(NodeConstructor childObject);

	public String defaultXmlTag();

}
