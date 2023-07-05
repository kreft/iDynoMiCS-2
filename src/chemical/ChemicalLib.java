package chemical;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.Log;
import dataIO.Log.Tier;
import instantiable.Instantiable;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;

public class ChemicalLib implements Instantiable, Settable
{
	private HashMap<String,Chemical> _chemicals = 
			new HashMap<String,Chemical>();
	private Settable _parentNode;

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		Log.out(Tier.NORMAL, "Chemical Library loading...");
		/* 
		 * Cycle through all chemicals and add them to the library.
		 */ 
		NodeList nodes = xmlElement.getElementsByTagName(XmlRef.chemical);
		Element chemElem;
		for ( int i = 0; i < nodes.getLength(); i++ ) 
		{
			chemElem = (Element) nodes.item(i);
			this.set(chemElem);
		}
		Log.out(Tier.NORMAL, "Chemical Library loaded!\n");
	}
	
	public void set(Element chem)
	{
		Chemical out = new Chemical();
		out.instantiate(chem, this);
		out.setParent(this);
		this._chemicals.put(out.getName(), out);
	}
	
	public Chemical get(String chemical)
	{
		return this._chemicals.get(chemical);
	}
	
	public void remove(Chemical chem)
	{
		this._chemicals.remove(chem);
	}

	@Override
	public Module getModule()
	{
		/* the Chemical lib node */
		Module modelNode = new Module(XmlRef.chemicalLibrary, this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		
		/* Species constructor */
		modelNode.addChildSpec( ClassRef.chemical,
				Module.Requirements.ZERO_TO_MANY);
		
		/* the already existing species */
		for ( String s : this._chemicals.keySet() )
			modelNode.add(((Chemical) _chemicals.get(s)).getModule());
	
		return modelNode;
	}

	@Override
	public String defaultXmlTag()
	{
		return XmlRef.chemicalLibrary;
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
