package instantiatable.object;

import org.w3c.dom.Element;

import dataIO.ObjectFactory;
import instantiatable.Instantiatable;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;

public class ListEntry<T> implements Settable, Instantiatable {
	
	/**
	 * 
	 */
	public T mapObject;
	public InstantiatableList<T> pile;
	private Settable _parentNode;
	
	public ListEntry(InstantiatableList<T> pile, T object )
	{
		this.pile = pile;
		this.mapObject = object;
	}
	
	public ListEntry()
	{
		// NOTE for instatniatable interface
	}

	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElem, Settable parent)
	{
		this.pile = (InstantiatableList<T>) parent;
		this.pile.add(this.mapObject);
	}
	
	public Module getModule() 
	{
		Module modelNode = new Module(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if (mapObject == null)
			modelNode.add(new Attribute( pile.valueLabel, 
					"", null, true));
		else
			modelNode.add(new Attribute( pile.valueLabel, 
					String.valueOf(mapObject), null, true));
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setModule(Module node)
	{
		this.pile.remove( this.mapObject );
		
		this.mapObject = (T) ObjectFactory.loadObject(
				node.getAttribute( pile.valueLabel ).getValue(), 
				pile.entryClass.getSimpleName() );

		this.pile.add( this.mapObject );

		Settable.super.setModule(node);
	}

	public void removeModule(String specifier)
	{
		this.pile.remove(this.mapObject);
	}
	
	
	

	@Override
	public String defaultXmlTag() 
	{
		return pile.nodeLabel;
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