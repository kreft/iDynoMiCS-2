package instantiable.object;

import org.w3c.dom.Element;

import dataIO.ObjectFactory;
import instantiable.Instantiable;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;

public class MapEntry<K, T> implements Settable, Instantiable {

	/**
	 * 
	 */
	public T mapObject;
	public K mapKey;
	public InstantiableMap<K,T> map;
	private Settable _parentNode;
	
	public MapEntry(T object, K key, InstantiableMap<K,T> map )
	{
		this.mapObject = object;
		this.map = map;
		this.mapKey = key;
	}
	
	public MapEntry()
	{
		// NOTE for instantiatable interface
	}

	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElem, Settable parent)
	{
		this.map = (InstantiableMap<K,T>) parent;
		this.map.put(this.mapKey, this.mapObject);
	}
	
	public Module getModule() 
	{
		Module modelNode = new Module(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);

		if (mapKey == null)
			modelNode.add(new Attribute(map.keyLabel, 
					"", null, true));
		else
			modelNode.add(new Attribute(map.keyLabel, 
					String.valueOf(mapKey), null, true));

		if (mapObject == null)
			modelNode.add(new Attribute(map.valueLabel, 
					"", null, true));
		else
			modelNode.add(new Attribute(map.valueLabel, 
					String.valueOf(mapObject), null, true));
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setModule(Module node)
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

		Settable.super.setModule(node);
	}

	public void removeModule(String specifier)
	{
		this.map.remove(this.mapKey);
	}

	@Override
	public String defaultXmlTag() 
	{
		return map.nodeLabel;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}