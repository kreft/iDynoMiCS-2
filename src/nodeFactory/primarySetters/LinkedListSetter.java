package nodeFactory.primarySetters;

import java.util.List;

import dataIO.XmlRef;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;

public class LinkedListSetter<T> implements NodeConstructor {
	
	public Object listObject;
	public List<T> list;
	
	public LinkedListSetter(Object object, List<T> list )
	{
		this.listObject = object;
		this.list = list;
	}

	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.setTitle(": list");
		
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
	public NodeConstructor newBlank() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void removeNode(String specifier)
	{
		this.list.remove(this.listObject);
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.item;
	}
}
