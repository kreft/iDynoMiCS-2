package nodeFactory.primarySetters;

import java.util.List;

import dataIO.ObjectFactory;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;
import nodeFactory.NodeConstructor;

public class LinkedListSetter<T> implements NodeConstructor {
	
	public Object listObject;
	public List<T> list;
	
	public String valueClassLabel;
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteClassDef = false;
	
	public LinkedListSetter(Object object, List<T> list )
	{
		this.listObject = object;
		this.list = list;
		
		this.valueClassLabel = XmlRef.classAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.nodeLabel = XmlRef.item;
	}
	
	public LinkedListSetter(Object object, List<T> list,
			String valueClass, String valueAttribute, String nodeLabel)
	{
		this.listObject = object;
		this.list = list;
		
		this.valueClassLabel = valueClass;
		this.valueLabel = valueAttribute;
		
		this.nodeLabel = XmlRef.item;
		this.muteClassDef = true;
	}

	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if ( !muteClassDef )
			modelNode.add(new ModelAttribute( this.valueClassLabel , 
					listObject.getClass().getSimpleName(), null, true ));
		
		if (listObject instanceof NodeConstructor)
		{
			modelNode.add(((NodeConstructor) listObject).getNode()); 
		}
		else
		{
			modelNode.add(new ModelAttribute( this.valueLabel, 
					String.valueOf(listObject), null, true));
		}
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setNode(ModelNode node)
	{
		Object  value;
		if (this.listObject instanceof NodeConstructor)
		{
			value = node.getAllChildNodes().get(0).constructor;
		}
		else
		{
			if ( this.muteClassDef )
			{
				value = ObjectFactory.loadObject(
						node.getAttribute( this.valueLabel ).value, 
						this.valueClassLabel );
			}
			else
			{
				value = ObjectFactory.loadObject(
						node.getAttribute( this.valueLabel ).value, 
						node.getAttribute( this.valueClassLabel ).value  );
			}
		}
		if ( this.list.contains( value ) )
			this.list.remove( value );
		this.list.add((T) value );

		NodeConstructor.super.setNode(node);
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
