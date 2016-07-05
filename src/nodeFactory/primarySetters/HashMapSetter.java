package nodeFactory.primarySetters;

import java.util.Map;

import dataIO.ObjectFactory;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;
import nodeFactory.NodeConstructor;

public class HashMapSetter<K,T> implements NodeConstructor {

	public Object mapObject;
	public Object mapKey;
	public Map<K,T> map;
	
	public String keyClassLabel;
	public String valueClassLabel;
	public String keyLabel;
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteClassDef = false;
	
	public HashMapSetter(Object object, Object key, Map<K,T> map )
	{
		this.mapObject = object;
		this.map = map;
		this.mapKey = key;
		
		this.keyClassLabel = XmlRef.keyClassAttribute;
		this.keyLabel = XmlRef.keyAttribute;
		this.valueClassLabel = XmlRef.classAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.nodeLabel = XmlRef.item;
	}
	
	public HashMapSetter(Object object, Object key, Map<K,T> map,
			String keyClass, String keyAttribute, String valueClass, 
			String valueAttribute, String nodeLabel)
	{
		this.mapObject = object;
		this.map = map;
		this.mapKey = key;
		
		this.keyClassLabel = keyClass;
		this.keyLabel = keyAttribute;
		this.valueClassLabel = valueClass;
		this.valueLabel = valueAttribute;
		
		this.nodeLabel = nodeLabel;
		this.muteClassDef = true;
	}

	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if ( !muteClassDef )
			modelNode.add(new ModelAttribute(keyClassLabel, 
					mapKey.getClass().getSimpleName(), null, true ));
		
		if (mapObject instanceof NodeConstructor)
		{
			modelNode.add(((NodeConstructor) mapKey).getNode()); 
		}
		else
		{
			modelNode.add(new ModelAttribute(keyLabel, 
					String.valueOf(mapKey), null, true));
		}
		
		if ( !muteClassDef )
			modelNode.add(new ModelAttribute(valueClassLabel, 
					mapObject.getClass().getSimpleName(), null, true ));
			
		if (mapObject instanceof NodeConstructor)
		{
			modelNode.add(((NodeConstructor) mapObject).getNode()); 
		}
		else
		{
			modelNode.add(new ModelAttribute(valueLabel, 
					String.valueOf(mapObject), null, true));
		}
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setNode(ModelNode node)
	{
		Object key, value;
		if (this.mapObject instanceof NodeConstructor)
		{
			value = node.getAllChildNodes().get(0).constructor;
			if ( this.muteClassDef )
			{
				key = ObjectFactory.loadObject(
						node.getAttribute( this.keyLabel ).getValue() , 
						this.keyClassLabel );
			}
			else
			{
			key = ObjectFactory.loadObject(
					node.getAttribute( this.keyLabel ).getValue() , 
					node.getAttribute( this.keyClassLabel ).getValue() );
			}
		}
		else
		{
			if ( this.muteClassDef )
			{
				key = ObjectFactory.loadObject(
						node.getAttribute( this.keyLabel ).getValue() , 
						this.keyClassLabel );
				value = ObjectFactory.loadObject(
						node.getAttribute( this.valueLabel ).getValue(), 
						this.valueClassLabel );
			}
			else
			{
			key = ObjectFactory.loadObject(
					node.getAttribute( this.keyLabel ).getValue() , 
					node.getAttribute( this.keyClassLabel ).getValue() );
			value = ObjectFactory.loadObject(
					node.getAttribute( this.valueLabel ).getValue(), 
					node.getAttribute( this.valueClassLabel ).getValue()  );
			}
		}
		if ( this.map.containsKey( key ) )
			this.map.remove( key );
		this.map.put( (K) key, (T) value );

		NodeConstructor.super.setNode(node);
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
		return this.nodeLabel;
	}
}
