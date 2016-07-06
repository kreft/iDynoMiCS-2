package nodeFactory.primarySetters;

import dataIO.ObjectFactory;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;

public class BundleEntry<K, T> implements NodeConstructor {

	/**
	 * 
	 */
	public T mapObject;
	public K mapKey;
	public Bundle<K,T> map;
	
	public BundleEntry(T object, K key, Bundle<K,T> map )
	{
		this.mapObject = object;
		this.map = map;
		this.mapKey = key;
	}
	
	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);

		modelNode.add(new ModelAttribute(map.keyLabel, 
				String.valueOf(mapKey), null, true));

		modelNode.add(new ModelAttribute(map.valueLabel, 
				String.valueOf(mapObject), null, true));

		if ( !this.map.muteClassDef )
		{
			modelNode.add(new ModelAttribute(XmlRef.keyClassAttribute, 
					mapKey.getClass().getSimpleName(), null, false ));

			modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
					mapObject.getClass().getSimpleName(), null, false ));
		}
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setNode(ModelNode node)
	{
		Object key, value;

		key = ObjectFactory.loadObject(
				node.getAttribute( map.keyLabel ).getValue(), 
				mapKey.getClass().getSimpleName() );
		value = ObjectFactory.loadObject(
				node.getAttribute( map.valueLabel ).getValue(), 
				mapObject.getClass().getSimpleName() );

		if ( this.map.containsKey( key ) )
			this.map.remove( key );
		this.map.put( (K) key, (T) value );
	}

	public void removeNode(String specifier)
	{
		this.map.remove(this.mapKey);
	}

	@Override
	public String defaultXmlTag() 
	{
		return map.nodeLabel;
	}
}