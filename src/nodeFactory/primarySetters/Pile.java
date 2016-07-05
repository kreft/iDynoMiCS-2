package nodeFactory.primarySetters;

import java.util.LinkedList;

import dataIO.ObjectFactory;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <T>
 */
public class Pile<T> extends LinkedList<T> implements NodeConstructor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4490405234387168192L;
	
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteAttributeDef = false;
	public boolean muteClassDef = false;
	
	public Requirements requirement;

	private String dictionaryLabel;
	
	public Pile()
	{
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
	}
	
	
	public Pile(String valueAttribute, String dictionaryLabel, String nodeLabel)
	{
		this.valueLabel = valueAttribute;
		this.dictionaryLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
	}

	@Override
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(dictionaryLabel, this);

		modelNode.setRequirements(requirement);

		if ( !muteAttributeDef )
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					this.valueLabel, null, true));

		for ( T entry : this) 
			modelNode.add(new Entry(entry, this ).getNode());
		return modelNode;
	}

	@Override
	public String defaultXmlTag() 
	{
		return this.dictionaryLabel;
	}
	
	public class Entry implements NodeConstructor {

		public T mapObject;
		public Pile<T> list;
		
		public Entry(T object, Pile<T> list )
		{
			this.mapObject = object;
			this.list = list;
		}
		
		public ModelNode getNode() 
		{
			ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
			modelNode.setRequirements(Requirements.ZERO_TO_MANY);
			modelNode.add(new ModelAttribute(list.valueLabel, 
					String.valueOf(mapObject), null, true));

			if ( !muteClassDef )
			{
				modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
						mapObject.getClass().getSimpleName(), null, false ));
			}
			
			return modelNode;
		}
		
		@SuppressWarnings("unchecked")
		public void setNode(ModelNode node)
		{
			this.list.remove( mapObject );

			Object value = ObjectFactory.loadObject(
					node.getAttribute( list.valueLabel ).value, 
					mapObject.getClass().getSimpleName() );

			this.list.add( (T) value );
		}

		public void removeNode(String specifier)
		{
			this.list.remove(this.mapObject);
		}

		@Override
		public String defaultXmlTag() 
		{
			return list.nodeLabel;
		}
	}


}
