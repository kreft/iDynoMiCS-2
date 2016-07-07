package nodeFactory.primarySetters;

import org.w3c.dom.Element;

import dataIO.ObjectFactory;
import generalInterfaces.Instantiatable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;

public class BundleEntry<K, T> implements NodeConstructor, Instantiatable {

	/**
	 * 
	 */
	public T mapObject;
	public K mapKey;
	public Bundle<K,T> map;
	private NodeConstructor _parentNode;
	
	public BundleEntry(T object, K key, Bundle<K,T> map )
	{
		this.mapObject = object;
		this.map = map;
		this.mapKey = key;
	}
	
	public BundleEntry()
	{
		// NOTE for instatniatable interface
	}

	@SuppressWarnings("unchecked")
	public void init(Element xmlElem, NodeConstructor parent)
	{
		this.map = (Bundle<K,T>) parent;
		this.map.put(this.mapKey, this.mapObject);
	}
	
	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);

		if (mapKey == null)
			modelNode.add(new ModelAttribute(map.keyLabel, 
					"", null, true));
		else
			modelNode.add(new ModelAttribute(map.keyLabel, 
					String.valueOf(mapKey), null, true));

		if (mapObject == null)
			modelNode.add(new ModelAttribute(map.valueLabel, 
					"", null, true));
		else
			modelNode.add(new ModelAttribute(map.valueLabel, 
					String.valueOf(mapObject), null, true));
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setNode(ModelNode node)
	{
		this.map.remove( this.mapKey );
		this.mapKey = (K) ObjectFactory.loadObject(
				node.getAttribute( map.keyLabel ).getValue(), 
				map.keyClass.getSimpleName() );
		this.mapObject = (T) ObjectFactory.loadObject(
				node.getAttribute( map.valueLabel ).getValue(), 
				map.entryClass.getSimpleName() );

		this.map.remove( this.mapKey );
		this.map.put( (K) this.mapKey, (T) this.mapObject );

		NodeConstructor.super.setNode(node);
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

	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
}