package settable.primarySetters;

import java.util.Map;

import dataIO.ObjectFactory;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;

public class HashMapSetter<K,T> implements Settable {

	public Object mapObject;
	public Object mapKey;
	public Map<K,T> map;
	
	public String keyClassLabel;
	public String valueClassLabel;
	public String keyLabel;
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteClassDef = false;
	private Settable _parentNode;
	
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

	public Module getModule() 
	{
		Module modelNode = new Module(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if ( !muteClassDef )
			modelNode.add(new Attribute(keyClassLabel, 
					mapKey.getClass().getSimpleName(), null, true ));
		
		if (mapObject instanceof Settable)
		{
			modelNode.add(((Settable) mapKey).getModule()); 
		}
		else
		{
			modelNode.add(new Attribute(keyLabel, 
					String.valueOf(mapKey), null, true));
		}
		
		if ( !muteClassDef )
			modelNode.add(new Attribute(valueClassLabel, 
					mapObject.getClass().getSimpleName(), null, true ));
			
		if (mapObject instanceof Settable)
		{
			modelNode.add(((Settable) mapObject).getModule()); 
		}
		else
		{
			modelNode.add(new Attribute(valueLabel, 
					ObjectFactory.stringRepresentation(mapObject), null, true));
		}
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setModule(Module node)
	{
		Object key, value;
		if (this.mapObject instanceof Settable)
		{
			value = node.getAllChildModules().get(0).getAssociatedObject();
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

		Settable.super.setModule(node);
	}
	
	public void removeModule(String specifier)
	{
		this.map.remove(this.mapKey);
	}


	@Override
	public String defaultXmlTag() 
	{
		return this.nodeLabel;
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
