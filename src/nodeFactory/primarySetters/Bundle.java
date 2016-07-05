package nodeFactory.primarySetters;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

import dataIO.ObjectFactory;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import referenceLibrary.XmlRef;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <K>
 * @param <T>
 */
public class Bundle<K,T> extends HashMap<K,T> implements NodeConstructor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4490405234387168192L;
	
	public String keyLabel;
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteAttributeDef = false;
	public boolean muteClassDef = false;

	private String dictionaryLabel;
	
	public Bundle()
	{
		this.keyLabel = XmlRef.keyAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
	}
	
	
	public Bundle(String keyAttribute, 
			String valueAttribute, String dictionaryLabel, String nodeLabel)
	{
		this.keyLabel = keyAttribute;
		this.valueLabel = valueAttribute;
		
		this.dictionaryLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;

	}

	@Override
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(dictionaryLabel, this);

		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if ( !muteAttributeDef )
			modelNode.add(new ModelAttribute(XmlRef.keyAttribute, 
					this.keyLabel, null, true));
		
		if ( !muteAttributeDef )
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					this.valueLabel, null, true));

		for ( K key : this.keySet() )
			modelNode.add(new Entry(this.get(key), key, this ).getNode());
		return modelNode;
	}

	@Override
	public String defaultXmlTag() 
	{
		return this.dictionaryLabel;
	}
	
	public class Entry implements NodeConstructor {

		public T mapObject;
		public K mapKey;
		public Bundle<K,T> map;
		
		public Entry(T object, K key, Bundle<K,T> map )
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

			if ( !muteClassDef )
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


}
