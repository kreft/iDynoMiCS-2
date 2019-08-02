package instantiable.object;

import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import idynomics.Idynomics;
import instantiable.Instance;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import utility.Helper;
import settable.Module.Requirements;

/**
 * The Bundle Map extends the Java HashMap and implements the iDynoMiCS
 * NodeConstructor and Instantiable class to provide easy management of maps
 * with the GUI and XML output.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <K>
 * @param <T>
 */
public class InstantiableMap<K,T> extends HashMap<K,T> implements Settable, 
		Instantiable, Copyable
{
	/**
	 * default serial uid (generated)
	 */
	private static final long serialVersionUID = 4490405234387168192L;
	
	/**
	 * the label of the key attribute
	 */
	public String keyLabel;
	
	/**
	 * the label of the value attribute
	 */
	public String valueLabel;
	
	/**
	 * the label of the (xml) node
	 */
	public String nodeLabel;
	
	/**
	 * boolean mutes class and label definition in xml and gui used by classes
	 * that always implement default labeling ( thus not settable by user )
	 */
	public boolean muteSpecification = false;

	/**
	 * The (xml) node label of the Bundle itself.
	 */
	private String bundleMapLabel;
	
	/**
	 * the parentNode of this pileList
	 */
	protected Settable _parentNode;
	
	/**
	 * The class of keys associated with the entries stored in the map
	 */
	public Class<?> keyClass;
	
	/**
	 * The class of entries stored in the map
	 */
	public Class<?> entryClass;
	
	/**
	 * ModelNode requirement definition
	 */
	public Requirements requirement = Requirements.IMMUTABLE;
	
	/**
	 * Constructor for bundle with default settings
	 * 
	 * @param entryClass
	 * @param keyClass
	 */
	public InstantiableMap( Class<?> keyClass, Class<?> entryClass )
	{
		this.keyLabel = XmlRef.keyAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.bundleMapLabel = XmlRef.map;
		this.nodeLabel = XmlRef.item;
		
		this.keyClass = keyClass;
		this.entryClass = entryClass;
	}
	
	/**
	 * Constructor, overwriting default (xml) attribute and node labels
	 *
	 * @param keyClass
	 * @param entryClass
	 * @param keyAttribute
	 * @param valueAttribute
	 * @param dictionaryLabel
	 * @param nodeLabel
	 */
	public InstantiableMap( Class<?> keyClass, Class<?> entryClass, String keyAttribute, 
			String valueAttribute, String dictionaryLabel, String nodeLabel )
	{
		this.keyLabel = keyAttribute;
		this.valueLabel = valueAttribute;
		
		this.bundleMapLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
		
		this.keyClass = keyClass;
		this.entryClass = entryClass;
	}
	
	/**
	 * Constructor, overwriting default (xml) attribute and node labels, but
	 * muting the specifications for xml output and gui
	 *
	 * @param keyClass
	 * @param entryClass
	 * @param keyAttribute
	 * @param valueAttribute
	 * @param dictionaryLabel
	 * @param nodeLabel
	 * @param muteSpec
	 */
	public InstantiableMap( Class<?> keyClass, Class<?> entryClass, String keyAttribute, 
			String valueAttribute, String dictionaryLabel, String nodeLabel, 
			boolean muteSpec )
	{
		this( keyClass, entryClass, keyAttribute, valueAttribute, 
				dictionaryLabel, nodeLabel );
		muteSpecification = muteSpec;
	}
	
	/**
	 * BundleMap constructor for Instantiable interface
	 */
	public InstantiableMap()
	{
		// NOTE only for Instantiable interface
	}
	

	@SuppressWarnings("unchecked")
	public Object copy() 
	{
		InstantiableMap<K,T> out = new InstantiableMap<K,T>( this.keyClass,
				this.entryClass, this.keyLabel,
				this.valueLabel, this.bundleMapLabel, this.nodeLabel );
		for(Object key : this.keySet())
			out.put((K) key, (T) ObjectFactory.copy(this.get((K) key)));
		return out;
	}
	
	
	public void instantiate(Element xmlElement, Settable parent)
	{
		this.instantiate(xmlElement, parent, null, null, null);
	}
	
	/**
	 * \brief instantiate from xml or gui
	 * 
	 * Note here we allow to pass a key and string defenition by forhand in case
	 * of Maps that require to have specific key and value types (for example
	 * the reaction stochiometric map).
	 */
	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElement, Settable parent, 
			String keyClass, String entryClass, String nodeLabel)
	{
		if( xmlElement == null )
		{
			try {
				this.keyClass = Class.forName( Idynomics.xmlPackageLibrary.
						getFull( Helper.obtainInput( keyClass , " object class of "
								+ "key") ) );
				this.entryClass = Class.forName( Idynomics.xmlPackageLibrary.
						getFull( Helper.obtainInput( entryClass , " object class of "
								+ "entries") ) );
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.bundleMapLabel = XmlRef.map;
			
			this.nodeLabel = Helper.obtainInput( nodeLabel , "entry node label");
			
			if ( ! Settable.class.isAssignableFrom(this.entryClass) )
			{
				this.valueLabel = XmlRef.valueAttribute;
			}
			if ( ! Settable.class.isAssignableFrom(this.keyClass) )
			{
				this.keyLabel = XmlRef.keyAttribute;
			}
		}
		else
		{
			if (this.bundleMapLabel == null ){
				if ( XmlHandler.hasAttribute(xmlElement, XmlRef.nameAttribute))
					this.bundleMapLabel = XmlRef.map;
				else
					this.bundleMapLabel = xmlElement.getNodeName();
				
			}
			
			if (XmlHandler.hasChild(xmlElement, this.bundleMapLabel))
				xmlElement = XmlHandler.findUniqueChild(xmlElement, this.bundleMapLabel);
			
			if (this.keyLabel == null)
			{
				this.keyLabel = XmlHandler.gatherAttribute(xmlElement, 
						XmlRef.keyAttribute);
				if (this.keyLabel == null)
				{
					this.muteSpecification = false;
					this.keyLabel = XmlRef.keyAttribute;
				}
			}
			
			if (this.valueLabel == null)
			{
				this.valueLabel = XmlHandler.gatherAttribute( xmlElement, 
						XmlRef.valueAttribute );
				if (this.valueLabel == null)
				{
					this.muteSpecification = false;
					this.valueLabel = XmlRef.valueAttribute;
				}
			}
			
			if (this.nodeLabel == null)
			{
				this.nodeLabel = XmlHandler.gatherAttribute(xmlElement, 
						XmlRef.nodeLabel);
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
							this.bundleMapLabel ) ) );
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			if (this.keyClass == null)
			{
				try {
					this.keyClass = Class.forName( 
							Idynomics.xmlPackageLibrary.getFull(
							XmlHandler.obtainAttribute(	xmlElement, 
							XmlRef.keyClassAttribute, 
							this.bundleMapLabel ) ) );
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
							this.valueLabel, this.entryClass.getSimpleName(), null, null ); 
					if( object instanceof Settable )
						((Settable) object).setParent(this);
					this.put((K) ObjectFactory.loadObject( (Element) nodes.item(i), 
							this.keyLabel, this.keyClass.getSimpleName(), null, null ),
							object );
				}
			}
		}
	}

	/**
	 * returns the model node of this BundleMap
	 * @return ModelNode
	 */
	@Override
	public Module getModule() {
		
		Module modelNode = new Module(bundleMapLabel, this);
		modelNode.setRequirements(requirement);
		
		if( !muteSpecification)
		{
			modelNode.add(new Attribute(XmlRef.nodeLabel, 
					this.nodeLabel, null, false ));
	
			if ( this.valueLabel != null )
				modelNode.add(new Attribute(XmlRef.valueAttribute, 
						this.valueLabel, null, true));
			
			modelNode.add(new Attribute(XmlRef.entryClassAttribute, 
					this.entryClass.getSimpleName(), null, false ));
	
			if ( this.keyLabel != null )
				modelNode.add(new Attribute(XmlRef.keyAttribute, 
						this.keyLabel, null, true));
			
			modelNode.add(new Attribute(XmlRef.keyClassAttribute, 
					this.keyClass.getSimpleName(), null, false ));
		}

		if (Settable.class.isAssignableFrom(entryClass))
		{
			for ( K key : this.keySet() )
				modelNode.add(((Settable) this.get(key)).getModule());

			modelNode.addChildSpec( entryClass.getName(),
					Module.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		else
		{
			for ( K key : this.keySet() )
				modelNode.add( new MapEntry<K, T>( this.get(key), key, this ).getModule());
			
			modelNode.addChildSpec( MapEntry.class.getName(),
					Module.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		return modelNode;
	}

	/**
	 * returns the currently set Node label of this bundle list
	 * 
	 * @return String xml node label
	 */
	@Override
	public String defaultXmlTag() 
	{
		return this.bundleMapLabel;
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
