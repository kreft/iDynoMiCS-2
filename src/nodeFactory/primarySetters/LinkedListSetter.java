package nodeFactory.primarySetters;

import dataIO.XmlRef;
import generalInterfaces.XMLable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;

public class LinkedListSetter implements NodeConstructor {
	
	public Object listObject;
	
	public LinkedListSetter(Object object)
	{
		this.listObject = object;
	}

	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode("item", this);
		modelNode.requirement = Requirements.ZERO_TO_MANY;
		
		modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
				listObject.getClass().getSimpleName(), null, true ));
		
		if (listObject instanceof NodeConstructor)
		{
			modelNode.add(((NodeConstructor) listObject).getNode()); 
		}
		else
		{
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					String.valueOf(listObject), null, true));
		}
		
		return modelNode;
	}


	@Override
	public void setNode(ModelNode node) 
	{
		for(ModelNode n : node.childNodes)
			n.constructor.setNode(n);
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
