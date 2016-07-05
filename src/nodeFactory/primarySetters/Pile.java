package nodeFactory.primarySetters;

import java.util.LinkedList;

import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;

/**
 *TODO move item spec up to Pile level
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
	
	public Requirements requirement = Requirements.ZERO_TO_MANY;

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
		{
			if (entry instanceof NodeConstructor)
				modelNode.add(((NodeConstructor) entry).getNode());
			else
				modelNode.add(new PileEntry<T>( this, entry ).getNode());
		}
		
		modelNode.addConstructable( PileEntry.class.getName(),
				ModelNode.Requirements.ZERO_TO_MANY, this.nodeLabel);
		
		return modelNode;
	}
	

	@Override
	public String defaultXmlTag() 
	{
		return this.dictionaryLabel;
	}
}
