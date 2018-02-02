package chemical;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.Log;
import dataIO.Log.Tier;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Settable;

public class ChemicalLib implements Instantiable, Settable
{
	private HashMap<String,Chemical> _chemicals = 
			new HashMap<String,Chemical>();

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
		this._chemicals.put(out.getName(), out);
	}

	@Override
	public Module getModule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(Settable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Settable getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
