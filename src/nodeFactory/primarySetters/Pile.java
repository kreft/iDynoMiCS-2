package nodeFactory.primarySetters;

import java.util.LinkedList;

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
	public Class<?> entryClass;
	
	public Requirements requirement = Requirements.ZERO_TO_MANY;

	private String dictionaryLabel;
	
	public Pile(Class<?> entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		this.entryClass = entryClass;
	}
	
	public Pile(String entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		
		try {
			this.entryClass = Class.forName(entryClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public Pile(Class<?> entryClass, String valueAttribute, String dictionaryLabel, String nodeLabel)
	{
		this.valueLabel = valueAttribute;
		this.dictionaryLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
		this.entryClass = entryClass;
	}

	@Override
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(dictionaryLabel, this);
		modelNode.setRequirements(requirement);

		if ( !muteAttributeDef )
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					this.valueLabel, null, true));

		if (NodeConstructor.class.isAssignableFrom(entryClass))
		{
			for ( T entry : this) 
				modelNode.add(((NodeConstructor) entry).getNode());

			modelNode.addConstructable( entryClass.getName(),
					ModelNode.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		else
		{
			for ( T entry : this) 
				modelNode.add(new PileEntry<T>( this, entry ).getNode());
			
			modelNode.addConstructable( PileEntry.class.getName(),
					ModelNode.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		return modelNode;
	}
	

	@Override
	public String defaultXmlTag() 
	{
		return this.dictionaryLabel;
	}
}
