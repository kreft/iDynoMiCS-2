package nodeFactory.primarySetters;

import org.w3c.dom.Element;

import dataIO.ObjectFactory;
import generalInterfaces.Instantiatable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.ObjectRef;
import referenceLibrary.XmlRef;
import utility.Helper;

public class PileEntry<T> implements NodeConstructor, Instantiatable {

	/**
	 * 
	 */
	public T mapObject;
	public Pile<T> pile;
	
	public PileEntry(Pile<T> pile, T object )
	{
		this.pile = pile;
		this.mapObject = object;
	}
	
	public PileEntry()
	{
		this.pile = null;
		
	}
	
	@SuppressWarnings("unchecked")
	public void init(Element xmlElem, NodeConstructor parent)
	{
		this.pile = (Pile<T>) parent;
		this.mapObject = (T) ObjectFactory.loadObject(Helper.obtainInput("", "value"), classDef());

		this.pile.add(this.mapObject);
	}
	
	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.add(new ModelAttribute( pile.valueLabel, 
				String.valueOf(mapObject), null, true));

		if ( !this.pile.muteClassDef )
		{
			if ( mapObject == null )
			{
				modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
						this.classDef(), null, false ));
			}
			else
				modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
						mapObject.getClass().getSimpleName(), null, false ));
		}
		
		return modelNode;
	}
	
	private String classDef()
	{
		String classDef = null;
		for (T entry : pile)
		{
			if (entry != null)
			{
				classDef = entry.getClass().getSimpleName();
				break;
			}
		}
		if (classDef == null)
			return Helper.obtainInput(ObjectRef.getAllOptions(), "object type", false);
		else
			return classDef;
	}
	
	@SuppressWarnings("unchecked")
	public void setNode(ModelNode node)
	{
		this.pile.remove( mapObject );
		
		mapObject = (T) ObjectFactory.loadObject(
				node.getAttribute( pile.valueLabel ).getValue(), 
				node.getAttribute( XmlRef.classAttribute ).getValue() );

		this.pile.add( mapObject );
		NodeConstructor.super.setNode(node);
	}

	public void removeNode(String specifier)
	{
		this.pile.remove(this.mapObject);
	}

	@Override
	public String defaultXmlTag() 
	{
		return pile.nodeLabel;
	}
}