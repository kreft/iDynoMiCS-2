package reaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import chemical.Chemical;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import idynomics.Idynomics;
import instantiable.Instantiable;
import instantiable.object.InstantiableList;
import instantiable.object.InstantiableMap;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import utility.Helper;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class HalfReaction implements Instantiable, Copyable, Settable
{
	protected String _name;

	protected Settable _parentNode;

	private InstantiableMap<String,Double> _stoichiometry = 
			new InstantiableMap<String,Double>(	String.class, Double.class, 
			XmlRef.component, XmlRef.coefficient, XmlRef.stoichiometry, 
			XmlRef.stoichiometric, true);

	/**
	 * \brief Empty reaction for ReactionLibrary construction.
	 */
	public HalfReaction()
	{
		
	}

	public HalfReaction(Node xmlNode, Settable parent)
	{
		Element elem = (Element) xmlNode;
		this.instantiate( elem, parent );
	}

	public HalfReaction(
			Map<String,Double> stoichiometry, String name)
	{
		this._name = name;
		this._stoichiometry.putAll(stoichiometry);
	}

	@SuppressWarnings("unchecked")
	public void instantiate(Element xmlElem, Settable parent)
	{
		this._parentNode = parent;
		if (parent instanceof InstantiableList)
			((InstantiableList<HalfReaction>) parent).add(this);

		if ( !Helper.isNullOrEmpty(xmlElem) && 
				XmlHandler.hasChild(xmlElem, XmlRef.halfReaction))
		{
			xmlElem = XmlHandler.findUniqueChild(xmlElem, XmlRef.halfReaction);
		}
		
		this._name = XmlHandler.obtainAttribute(xmlElem, 
				XmlRef.nameAttribute, this.defaultXmlTag());
		/*
		 * Build the stoichiometric map.
		 */
		this._stoichiometry.instantiate( xmlElem, this, 
				String.class.getSimpleName(), Double.class.getSimpleName(), 
				XmlRef.stoichiometric );
	}
	
	public Object copy()
	{
		@SuppressWarnings("unchecked")
		HashMap<String,Double> stoichiometry = (HashMap<String,Double>)
				ObjectFactory.copy(this._stoichiometry);
		return new HalfReaction(stoichiometry,  this._name);
	}
	
	public Collection<String> getConstituentNames()
	{
		return getStoichiometry().keySet();
	}

	public double getStoichiometry(String reactantName)
	{
		if ( this._stoichiometry.containsKey(reactantName) )
			return this._stoichiometry.get(reactantName);
		return 0.0;
	}
	
	public double electrons()
	{
		/* TODO nicefy */
		return this.getStoichiometry("electron");
	}
	
	public Chemical maxCarbon()
	{
		Chemical chemical = null;
		double temp = 0.0;
		for( String chem : getConstituentNames() )
		{
			Chemical c = Idynomics.simulator.chemicalLibrary.get(chem);
			if( chemical == null || c.get("C") > temp)
				chemical = c;
		}
		return chemical;
	}

	public Map<String,Double> getStoichiometry()
	{
		return this._stoichiometry;
	}
	
	public Module getModule()
	{
		Module modelNode = new Module(XmlRef.halfReaction, this);

		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this._name);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this._name, null, false ));
		
		modelNode.add( this._stoichiometry.getModule() );

		return modelNode;
	}

	public void removeModule(String specifier)
	{
		Log.out(Tier.NORMAL, "Removal halfReaction is not implemented.");
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.halfReaction;
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
