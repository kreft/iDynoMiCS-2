package nodeFactory.primarySetters;

import java.util.Map;

import dataIO.XmlRef;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;

public class HashMapSetter<K,T> implements NodeConstructor {

	public Object mapObject;
	public Object mapKey;
	public Map<K,T> map;
	
	public HashMapSetter(Object object, Object key, Map<K,T> map )
	{
		this.mapObject = object;
		this.map = map;
		this.mapKey = key;
	}

	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.setTitle(": map");
		
		modelNode.add(new ModelAttribute(XmlRef.keyTypeAttribute, 
				mapKey.getClass().getSimpleName(), null, true ));
		
		if (mapObject instanceof NodeConstructor)
		{
			modelNode.add(((NodeConstructor) mapKey).getNode()); 
		}
		else
		{
			modelNode.add(new ModelAttribute(XmlRef.keyAttribute, 
					String.valueOf(mapKey), null, true));
		}
		
		modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
				mapObject.getClass().getSimpleName(), null, true ));
		
		if (mapObject instanceof NodeConstructor)
		{
			modelNode.add(((NodeConstructor) mapObject).getNode()); 
		}
		else
		{
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					String.valueOf(mapObject), null, true));
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
		this.map.remove(this.mapKey);
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
