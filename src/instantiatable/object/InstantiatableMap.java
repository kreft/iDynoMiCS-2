package instantiatable.object;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import instantiatable.Instantiatable;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;

/**
 * The Bundle Map extends the Java HashMap and implements the iDynoMiCS
 * NodeConstructor and Instantiatable class to provide easy management of maps
 * with the gui and xml output.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <K>
 * @param <T>
 */
public class InstantiatableMap<K,T> extends HashMap<K,T> implements Settable, Instantiatable
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
	public InstantiatableMap( Class<?> keyClass, Class<?> entryClass )
	{
		this.keyLabel = XmlRef.keyAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.bundleMapLabel = XmlRef.dictionary;
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
	public InstantiatableMap( Class<?> keyClass, Class<?> entryClass, String keyAttribute, 
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
	public InstantiatableMap( Class<?> keyClass, Class<?> entryClass, String keyAttribute, 
			String valueAttribute, String dictionaryLabel, String nodeLabel, 
			boolean muteSpec )
	{
		this( keyClass, entryClass, keyAttribute, valueAttribute, 
				dictionaryLabel, nodeLabel );
		muteSpecification = muteSpec;
	}
	
	/**
	 * BundleMap constructor for Instantiatable interface
	 */
	public InstantiatableMap()
	{
		// NOTE only for Instantiatable interface
	}
	
	/**
	 * Implementation of Instantiatable interface
	 * 
	 * TODO commenting
	 */
	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElement, Settable parent)
	{
		if (this.bundleMapLabel == null ){
			if ( XmlHandler.hasAttribute(xmlElement, XmlRef.nameAttribute))
				this.bundleMapLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.nameAttribute);
			else
				this.bundleMapLabel = xmlElement.getNodeName();
		}
		
		if (this.keyLabel == null)
		{
			this.keyLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.keyAttribute);
			if (this.keyLabel == null)
				this.muteSpecification = true;
		}
		
		if (this.valueLabel == null)
		{
			this.valueLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.valueAttribute);
			if (this.valueLabel == null)
				this.muteSpecification = true;
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
						XmlRef.entryClassAttribute, this.bundleMapLabel ) ) );
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if (this.keyClass == null)
		{
			try {
				this.keyClass = Class.forName( Idynomics.xmlPackageLibrary.getFull(
						XmlHandler.obtainAttribute(	xmlElement, 
						XmlRef.keyClassAttribute, this.bundleMapLabel ) ) );
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
						this.valueLabel, this.entryClass.getSimpleName() ); 
				if( object instanceof Settable )
					((Settable) object).setParent(this);
				this.put((K) ObjectFactory.loadObject( (Element) nodes.item(i), 
						this.keyLabel, this.keyClass.getSimpleName() ),
						object );
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
				modelNode.add( new Entry<K, T>( this.get(key), key, this ).getModule());
			
			modelNode.addChildSpec( Entry.class.getName(),
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
	 * add child nodeConstructor (map item)
	 * 
	 * FIXME todo
	 */
//	@SuppressWarnings("unchecked")
//	public void addChildObject(NodeConstructor childObject)
//	{
//		this.put((T) childObject);
//	}

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
	
	public class Entry<K, T> implements Settable, Instantiatable {

		/**
		 * 
		 */
		public T mapObject;
		public K mapKey;
		public InstantiatableMap<K,T> map;
		private Settable _parentNode;
		
		public Entry(T object, K key, InstantiatableMap<K,T> map )
		{
			this.mapObject = object;
			this.map = map;
			this.mapKey = key;
		}
		
		public Entry()
		{
			// NOTE for instantiatable interface
		}

		@SuppressWarnings("unchecked")
		public void instantiate(Element xmlElem, Settable parent)
		{
			this.map = (InstantiatableMap<K,T>) parent;
			this.map.put(this.mapKey, this.mapObject);
		}
		
		public Module getModule() 
		{
			Module modelNode = new Module(this.defaultXmlTag() , this);
			modelNode.setRequirements(Requirements.ZERO_TO_MANY);

			if (mapKey == null)
				modelNode.add(new Attribute(map.keyLabel, 
						"", null, true));
			else
				modelNode.add(new Attribute(map.keyLabel, 
						String.valueOf(mapKey), null, true));

			if (mapObject == null)
				modelNode.add(new Attribute(map.valueLabel, 
						"", null, true));
			else
				modelNode.add(new Attribute(map.valueLabel, 
						String.valueOf(mapObject), null, true));
			
			return modelNode;
		}
		
		@SuppressWarnings("unchecked")
		public void setModule(Module node)
		{
			this.map.remove( this.mapKey );
			this.mapKey = (K) ObjectFactory.loadObject(
					node.getAttribute( map.keyLabel ).getValue(), 
					map.keyClass.getSimpleName() );
			this.mapObject = (T) ObjectFactory.loadObject(
					node.getAttribute( map.valueLabel ).getValue(), 
					map.entryClass.getSimpleName() );

			this.map.remove( this.mapKey );
			this.map.put( (K) this.mapKey, (T) this.mapObject );

			Settable.super.setModule(node);
		}

		public void removeModule(String specifier)
		{
			this.map.remove(this.mapKey);
		}

		@Override
		public String defaultXmlTag() 
		{
			return map.nodeLabel;
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
}
