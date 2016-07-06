package nodeFactory.primarySetters;

import java.util.HashMap;

import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import referenceLibrary.XmlRef;
import nodeFactory.primarySetters.BundleEntry;

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
	
	public Class<?> keyClass;
	public Class<?> entryClass;
	
	public Bundle(Class<?> keyClass, Class<?> entryClass)
	{
		this.keyLabel = XmlRef.keyAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		
		this.keyClass = keyClass;
		this.entryClass = entryClass;
	}
	
	
	public Bundle(Class<?> keyClass, Class<?> entryClass, String keyAttribute, 
			String valueAttribute, String dictionaryLabel, String nodeLabel)
	{
		this.keyLabel = keyAttribute;
		this.valueLabel = valueAttribute;
		
		this.dictionaryLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
		
		this.keyClass = keyClass;
		this.entryClass = entryClass;
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
			modelNode.add( new BundleEntry<K, T>( this.get(key), key, this ).getNode());
		return modelNode;
	}

	@Override
	public String defaultXmlTag() 
	{
		return this.dictionaryLabel;
	}


}
