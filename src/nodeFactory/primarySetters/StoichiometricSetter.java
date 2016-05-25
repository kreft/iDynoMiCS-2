package nodeFactory.primarySetters;

import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;

public class StoichiometricSetter implements NodeConstructor {
	
	public Object stoichObject;
	
	public StoichiometricSetter(Object stoichiometricObject)
	{
		this.stoichObject = stoichiometricObject;
	}

	@Override
	public ModelNode getNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNode(ModelNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NodeConstructor newBlank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}

}
