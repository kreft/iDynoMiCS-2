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
 * The Pile list extends the Java LinkedList and implements the iDynoMiCS
 * NodeConstructor and Instantiatable class to provide easy management of lists
 * with the gui and xml output.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <T>
 */
public class PileList<T> extends LinkedList<T> implements NodeConstructor, 
		Instantiatable
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
	private NodeConstructor _parentNode;
	
	/**
	 * Constructor for pile with default settings
	 * 
	 * @param entryClass
	 */
	public PileList(Class<?> entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		this.pileListLabel = XmlRef.dictionary;
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
	public PileList(String entryClass)
	{
		this.valueLabel = XmlRef.valueAttribute;
		
		this.pileListLabel = XmlRef.dictionary;
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
	public PileList(Class<?> entryClass, String valueAttribute, 
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
	public PileList()
	{
		// NOTE only for Instantiatable interface
	}
	
	/**
	 * Implementation of Instantiatable interface
	 * 
	 * TODO commenting
	 */
	@SuppressWarnings("unchecked")
	public void init(Element xmlElement, NodeConstructor parent)
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
			this.pileListLabel = Helper.obtainInput( "" , ClassRef.pile + 
					" xml node");
			this.nodeLabel = Helper.obtainInput( "" , "pile entry xml node");
			
			if ( ! NodeConstructor.class.isAssignableFrom(entryClass) )
			{
				this.valueLabel = Helper.obtainInput( "" , " entry value "
						+ "label");
			}
		}
		else
		{
			if (this.pileListLabel == null ){
				if ( XmlHandler.hasAttribute(xmlElement, XmlRef.nameAttribute) )
					this.pileListLabel = XmlHandler.gatherAttribute( xmlElement, 
							XmlRef.nameAttribute );
				else
					this.pileListLabel = xmlElement.getNodeName();
			}
			
			xmlElement = XmlHandler.loadUnique(xmlElement, this.pileListLabel);
			
			if (this.valueLabel == null)
			{
				this.valueLabel = XmlHandler.gatherAttribute( xmlElement, 
						XmlRef.valueAttribute );
				if (this.valueLabel == null)
					this.muteAttributeDef = true;
			}
			
			if (this.nodeLabel == null)
			{
				this.nodeLabel = XmlHandler.gatherAttribute( xmlElement, 
						XmlRef.nodeLabel );
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
							null, this.entryClass.getSimpleName() );
					
					if( object instanceof NodeConstructor )
						((NodeConstructor) object).setParent(this);
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
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(pileListLabel, this);
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
	 * forward for easy use of the Instantiatable interface
	 * @param s
	 * @param parent
	 * @return
	 */
	public static Object getNewInstance(Element s, NodeConstructor parent) 
	{
		return Instantiatable.getNewInstance( PileList.class.getName(), s, 
				parent );
	}
	
	/**
	 * add child nodeConstructor (list item)
	 */
	@SuppressWarnings("unchecked")
	public void addChildObject(NodeConstructor childObject)
	{
		this.add((T) childObject);
	}

	/**
	 * set the parent node constructor of this pile list.
	 */
	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public NodeConstructor getParent() 
	{
		return this._parentNode;
	}
}
