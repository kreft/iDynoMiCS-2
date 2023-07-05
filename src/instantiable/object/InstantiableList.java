package instantiable.object;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import idynomics.Idynomics;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import utility.Helper;

/**
 * 
 * The Pile list extends the Java LinkedList and implements the iDynoMiCS
 * NodeConstructor and Instantiable class to provide easy management of lists
 * with the GUI and xml output.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <T>
 */
public class InstantiableList<T> extends LinkedList<T> implements Settable, 
		Instantiable, Copyable
{
	/**
	 * default serial uid (generated)
	 */
	private static final long serialVersionUID = 4490405234387168192L;
	
	/**
	 * the label of the value attribute
	 */
	public String valueLabel;
	
	/**
	 * the label of the (xml) node
	 */
	public String nodeLabel;
	
	/**
	 * TODO
	 */
	public boolean muteAttributeDef = false;
	
	/**
	 * boolean mutes class and label definition in xml and gui used by classes
	 * that always implement default labeling ( thus not settable by user )
	 */
	public boolean muteSpecification = false;
	
	/**
	 * The class of entries stored in the list
	 */
	public Class<?> entryClass;
	
	/**
	 * ModelNode requirement definition
	 */
	public Requirements requirement = Requirements.IMMUTABLE;

	/**
	 * The (xml) node label of the pile itself.
	 */
	private String pileListLabel;

	/**
	 * the parentNode of this pileList
	 */
	private Settable _parentNode;
	
	/**
	 * Constructor for pile with default settings
	 * 
	 * @param entryClass
	 */
	public InstantiableList(Class<?> entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		this.pileListLabel = XmlRef.InstantiableMapLable;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		this.entryClass = entryClass;
	}
	
	/**
	 * Constructor for pile with default settings, fetching entry class from 
	 * String input
	 * 
	 * @param entryClass
	 */
	public InstantiableList(String entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		
		this.pileListLabel = XmlRef.InstantiableMapLable;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		
		try {
			this.entryClass = Class.forName(entryClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor, overwriting default (xml) attribute and node labels
	 * 
	 * @param entryClass
	 * @param valueAttribute
	 * @param dictionaryLabel
	 * @param nodeLabel
	 */
	public InstantiableList(Class<?> entryClass, String valueAttribute, 
			String dictionaryLabel, String nodeLabel)
	{
		this.valueLabel = valueAttribute;
		if (this.valueLabel == null)
			this.muteAttributeDef = true;
		this.pileListLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
		this.entryClass = entryClass;
	}
	
	/**
	 * PileList constructor for Instantiatable interface
	 */
	public InstantiableList()
	{
		// NOTE only for Instantiatable interface
	}
	
	@SuppressWarnings("unchecked")
	public Object copy() 
	{
		InstantiableList<T> out = new InstantiableList<T>(this.entryClass,
				this.valueLabel, this.pileListLabel, this.nodeLabel );
		for(int i = 0; i < this.size(); i++)
			out.add((T) ObjectFactory.copy((this.get(i))));	
		return out;
	}
		
	/**
	 * Implementation of Instantiatable interface
	 * 
	 * TODO commenting
	 */
	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElement, Settable parent)
	{
		if( xmlElement == null )
		{
			try {
				this.entryClass = Class.forName( Idynomics.xmlPackageLibrary.
						getFull( Helper.obtainInput( "" , " object class of "
								+ "entries") ) );
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.pileListLabel = XmlRef.list;
			
			this.nodeLabel = Helper.obtainInput( "" , "entry node label");
			
			if ( ! Settable.class.isAssignableFrom(entryClass) )
			{
				this.valueLabel = XmlRef.valueAttribute;
			}
		}
		else
		{
			if (this.pileListLabel == null ){
				if ( XmlHandler.hasAttribute(xmlElement, XmlRef.nameAttribute) )
					this.pileListLabel = XmlRef.list;
				else
					this.pileListLabel = xmlElement.getNodeName();
			}
			
			if (XmlHandler.hasChild(xmlElement, this.pileListLabel))
				xmlElement = XmlHandler.findUniqueChild(xmlElement, this.pileListLabel);
			
			if (this.valueLabel == null)
			{
				this.valueLabel = XmlHandler.gatherAttribute( xmlElement, 
						XmlRef.valueAttribute );
				if (this.valueLabel == null)
				{
					this.muteAttributeDef = true;
					this.valueLabel = XmlRef.valueAttribute;
				}
			}
			
			if (this.nodeLabel == null)
			{
				this.nodeLabel = XmlHandler.gatherAttribute( xmlElement, 
						XmlRef.nodeLabel );
			}
			if (this.nodeLabel == null)
			{
				this.nodeLabel = XmlRef.item;
			}
			
			if (this.entryClass == null)
			{
				try {
					this.entryClass = Class.forName( 
							Idynomics.xmlPackageLibrary.getFull(
							XmlHandler.obtainAttribute(	xmlElement, 
							XmlRef.entryClassAttribute, 
							this.pileListLabel ) ) );
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			NodeList nodes = XmlHandler.getAll(xmlElement, this.nodeLabel);
			if (nodes != null)
			{
				for ( int i = 0; i < nodes.getLength(); i++ )
				{
					T object = (T) ObjectFactory.loadObject( 
							(Element) nodes.item(i), 
							this.valueLabel, this.entryClass.getSimpleName(), null, null );
					if( object instanceof Settable )
						((Settable) object).setParent(this);
					this.add( object );
				}
			}
		}
	}

	/**
	 * returns the model node of this PileList
	 * @return ModelNode
	 */
	@Override
	public Module getModule() {
		
		Module modelNode = new Module(pileListLabel, this);
		modelNode.setRequirements(requirement);
		
		modelNode.add(new Attribute(XmlRef.nodeLabel, 
				this.nodeLabel, null, false ));

		if ( this.valueLabel != null )
			modelNode.add(new Attribute(XmlRef.valueAttribute, 
					this.valueLabel, null, true));
		
		modelNode.add(new Attribute(XmlRef.entryClassAttribute, 
				this.entryClass.getSimpleName(), null, false ));

		if (Settable.class.isAssignableFrom(entryClass))
		{
			for ( T entry : this) 
				modelNode.add(((Settable) entry).getModule());

			modelNode.addChildSpec( entryClass.getName(),
					Module.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		else
		{
			for ( T entry : this) 
				modelNode.add(new ListEntry<T>( this, entry ).getModule());
			
			modelNode.addChildSpec( ListEntry.class.getName(),
					Module.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		return modelNode;
	}
	
	/**
	 * returns the currently set Node label of this pile list
	 * 
	 * @return String xml node label
	 */
	@Override
	public String defaultXmlTag() 
	{
		return this.pileListLabel;
	}

	/**
	 * add child nodeConstructor (list item)
	 */
	@SuppressWarnings("unchecked")
	public void addChildObject(Settable childObject)
	{
		this.add((T) childObject);
	}

	/**
	 * set the parent node constructor of this pile list.
	 */
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
