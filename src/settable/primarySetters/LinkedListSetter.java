package settable.primarySetters;

import java.util.List;

import dataIO.ObjectFactory;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;

public class LinkedListSetter<T> implements Settable {
	
	public Object listObject;
	public List<T> list;
	
	public String valueClassLabel;
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteClassDef = false;
	private Settable _parentNode;
	
	public LinkedListSetter(Object object, List<T> list )
	{
		this.listObject = object;
		this.list = list;
		
		this.valueClassLabel = XmlRef.classAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.nodeLabel = XmlRef.item;
	}
	
	public LinkedListSetter(Object object, List<T> list,
			String valueClass, String valueAttribute, String nodeLabel)
	{
		this.listObject = object;
		this.list = list;
		
		this.valueClassLabel = valueClass;
		this.valueLabel = valueAttribute;
		
		this.nodeLabel = XmlRef.item;
		this.muteClassDef = true;
	}

	public Module getModule() 
	{
		Module modelNode = new Module(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if ( !muteClassDef )
			modelNode.add(new Attribute( this.valueClassLabel , 
					listObject.getClass().getSimpleName(), null, true ));
		
		if (listObject instanceof Settable)
		{
			modelNode.add(((Settable) listObject).getModule()); 
		}
		else
		{
			modelNode.add(new Attribute( this.valueLabel, 
					ObjectFactory.stringRepresentation(listObject), null,true));
		}
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setModule(Module node)
	{
		Object  value;
		if (this.listObject instanceof Settable)
		{
			value = node.getAllChildModules().get(0).getAssociatedObject();
		}
		else
		{
			if ( this.muteClassDef )
			{
				value = ObjectFactory.loadObject(
						node.getAttribute( this.valueLabel ).getValue(), 
						this.valueClassLabel );
			}
			else
			{
				value = ObjectFactory.loadObject(
						node.getAttribute( this.valueLabel ).getValue(), 
						node.getAttribute( this.valueClassLabel ).getValue()  );
			}
		}
		if ( this.list.contains( value ) )
			this.list.remove( value );
		this.list.add((T) value );

		Settable.super.setModule(node);
	}
	
	public void removeModule(String specifier)
	{
		this.list.remove(this.listObject);
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.item;
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
