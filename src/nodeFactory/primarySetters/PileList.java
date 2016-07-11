package nodeFactory.primarySetters;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import generalInterfaces.Instantiatable;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <T>
 */
public class PileList<T> extends LinkedList<T> implements NodeConstructor, Instantiatable
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
	
	public Requirements requirement = Requirements.IMMUTABLE;

	private String dictionaryLabel;

	private NodeConstructor _parentNode;
	
	public PileList(Class<?> entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		this.entryClass = entryClass;
	}
	
	public PileList(String entryClass)
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
	
	
	public PileList(Class<?> entryClass, String valueAttribute, String dictionaryLabel, String nodeLabel)
	{
		this.valueLabel = valueAttribute;
		if (this.valueLabel == null)
			this.muteAttributeDef = true;
		this.dictionaryLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
		this.entryClass = entryClass;
	}
	
	public PileList()
	{
		// NOTE only for Instantiatable interface
	}
	
	@SuppressWarnings("unchecked")
	public void init(Element xmlElement, NodeConstructor parent)
	{
		if( xmlElement == null )
		{
			try {
				this.entryClass = Class.forName( Idynomics.xmlPackageLibrary.
						getFull( Helper.obtainInput( "" , " pile entry class.") ) );
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.dictionaryLabel = Helper.obtainInput( "" , ClassRef.pile + " xml node");
			this.nodeLabel = Helper.obtainInput( "" , "pile entry xml node");
			
			if ( ! NodeConstructor.class.isAssignableFrom(entryClass) )
			{
				this.valueLabel = Helper.obtainInput( "" , "pile entry value label");
			}
		}
		else
		{
			if (this.dictionaryLabel == null ){
				if ( XmlHandler.hasAttribute(xmlElement, XmlRef.nameAttribute))
					this.dictionaryLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.nameAttribute);
				else
					this.dictionaryLabel = xmlElement.getNodeName();
			}
			
			xmlElement = XmlHandler.loadUnique(xmlElement, this.dictionaryLabel);
			
			if (this.valueLabel == null)
			{
				this.valueLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.valueAttribute);
				if (this.valueLabel == null)
					this.muteAttributeDef = true;
			}
			
			if (this.nodeLabel == null)
			{
				this.nodeLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.nodeLabel);
			}
			
			if (this.entryClass == null)
			{
				try {
					this.entryClass = Class.forName( Idynomics.xmlPackageLibrary.getFull(
							XmlHandler.obtainAttribute(	xmlElement, 
							XmlRef.entryClassAttribute, this.dictionaryLabel ) ) );
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			NodeList nodes = XmlHandler.getAll(xmlElement, this.nodeLabel);
			if (nodes != null)
			{
				for ( int i = 0; i < nodes.getLength(); i++ )
				{
					T object = (T) ObjectFactory.loadObject( (Element) nodes.item(i), 
							null, this.entryClass.getSimpleName() );
					if( object instanceof NodeConstructor )
						((NodeConstructor) object).setParent(this);
					this.add( object );
				}
			}
		}
	}

	@Override
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(dictionaryLabel, this);
		modelNode.setRequirements(requirement);
		
		modelNode.add(new ModelAttribute(XmlRef.nodeLabel, 
				this.nodeLabel, null, false ));

		if ( this.valueLabel != null )
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					this.valueLabel, null, true));
		
		modelNode.add(new ModelAttribute(XmlRef.entryClassAttribute, 
				this.entryClass.getSimpleName(), null, false ));

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
	
	@Override
	public boolean add(T obj)
	{
		return super.add(obj);
	}

	public static Object getNewInstance(Element s, NodeConstructor parent) 
	{
		return Instantiatable.getNewInstance(PileList.class.getName(), s, parent);
	}
	
	@SuppressWarnings("unchecked")
	public void addChildObject(NodeConstructor childObject)
	{
		this.add((T) childObject);
	}

	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
}
