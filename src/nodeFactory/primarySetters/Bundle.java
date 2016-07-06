package nodeFactory.primarySetters;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import generalInterfaces.Instantiatable;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import referenceLibrary.XmlRef;
import nodeFactory.primarySetters.BundleEntry;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <K>
 * @param <T>
 */
public class Bundle<K,T> extends HashMap<K,T> implements NodeConstructor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4490405234387168192L;
	
	public String keyLabel;
	public String valueLabel;
	
	public String nodeLabel;
	public boolean muteAttributeDef = false;
	public boolean muteClassDef = false;

	private String dictionaryLabel;
	
	public Class<?> keyClass;
	public Class<?> entryClass;
	
	public Requirements requirement = Requirements.IMMUTABLE;
	
	public Bundle(Class<?> keyClass, Class<?> entryClass)
	{
		this.keyLabel = XmlRef.keyAttribute;
		this.valueLabel = XmlRef.valueAttribute;
		
		this.dictionaryLabel = XmlRef.dictionary;
		this.nodeLabel = XmlRef.item;
		this.muteAttributeDef = true;
		
		this.keyClass = keyClass;
		this.entryClass = entryClass;
	}
	
	
	public Bundle(Class<?> keyClass, Class<?> entryClass, String keyAttribute, 
			String valueAttribute, String dictionaryLabel, String nodeLabel)
	{
		this.keyLabel = keyAttribute;
		this.valueLabel = valueAttribute;
		
		this.dictionaryLabel = dictionaryLabel;
		this.nodeLabel = nodeLabel;
		
		this.keyClass = keyClass;
		this.entryClass = entryClass;
	}
	
	public Bundle()
	{
		// NOTE only for Instantiatable interface
		System.out.println("Inst");
	}
	
	@SuppressWarnings("unchecked")
	public void init(Element xmlElement, NodeConstructor parent)
	{
		if (this.dictionaryLabel == null ){
			if ( XmlHandler.hasAttribute(xmlElement, XmlRef.nameAttribute))
				this.dictionaryLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.nameAttribute);
			else
				this.dictionaryLabel = xmlElement.getNodeName();
		}
		
		if (this.keyLabel == null)
		{
			this.keyLabel = XmlHandler.gatherAttribute(xmlElement, XmlRef.keyAttribute);
			if (this.keyLabel == null)
				this.muteAttributeDef = true;
		}
		
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
		
		if (this.keyClass == null)
		{
			try {
				this.keyClass = Class.forName( Idynomics.xmlPackageLibrary.getFull(
						XmlHandler.obtainAttribute(	xmlElement, 
						XmlRef.keyClassAttribute, this.dictionaryLabel ) ) );
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		NodeList nodes = XmlHandler.getAll(xmlElement, this.nodeLabel);
		if (nodes != null)
		{
			for ( int i = 0; i < nodes.getLength(); i++ )
			{
				this.put((K) ObjectFactory.loadObject( (Element) nodes.item(i), 
						this.keyLabel, this.keyClass.getSimpleName() ),
						(T) ObjectFactory.loadObject( (Element) nodes.item(i), 
						this.valueLabel, this.entryClass.getSimpleName() ) );
			}
		}
	}

	@Override
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(dictionaryLabel, this);
		modelNode.setRequirements(requirement);
		
		if ( !muteAttributeDef )
			modelNode.add(new ModelAttribute(XmlRef.keyAttribute, 
					this.keyLabel, null, true));
		
		if ( !muteAttributeDef )
			modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
					this.valueLabel, null, true));
		

		if ( !this.muteClassDef )
		{
			modelNode.add(new ModelAttribute(XmlRef.keyClassAttribute, 
					this.keyClass.getSimpleName(), null, false ));

			modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
					this.entryClass.getSimpleName(), null, false ));
		}

		if (NodeConstructor.class.isAssignableFrom(entryClass))
		{
			for ( K key : this.keySet() )
				modelNode.add(((NodeConstructor) this.get(key)).getNode());

			modelNode.addConstructable( entryClass.getName(),
					ModelNode.Requirements.ZERO_TO_MANY, this.nodeLabel);
		}
		else
		{
			for ( K key : this.keySet() )
				modelNode.add( new BundleEntry<K, T>( this.get(key), key, this ).getNode());
		}
		return modelNode;
	}

	@Override
	public String defaultXmlTag() 
	{
		return this.dictionaryLabel;
	}

	public static Object getNewInstance(Element s, NodeConstructor parent) 
	{
		return Instantiatable.getNewInstance(Bundle.class.getName(), s, parent);
	}
}
