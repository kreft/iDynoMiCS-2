package dataIO;

import org.w3c.dom.Element;

import compartment.Compartment;
import idynomics.Idynomics;
import instantiable.Instance;
import processManager.ProcessManager;
import referenceLibrary.XmlRef;
import utility.Helper;

public class CumulativeLoad {
	
	Element document;

	public CumulativeLoad() 
	{
		
	}
	
	public CumulativeLoad(String xml) 
	{
		document = XmlHandler.loadDocument(xml);
	}
	
	public void postProcess()
	{
		Compartment comp = null;
		for ( Element e : XmlHandler.getElements( document, XmlRef.process) )
		{
			comp.addProcessManager(
					(ProcessManager) Instance.getNew(e, comp, (String[])null));
		}
	}
		
	public Compartment getCompartment(String comp)
	{
		if( Idynomics.simulator.getCompartmentNames().contains(comp))
			return Idynomics.simulator.getCompartment(comp);
		if( Helper.intParseable(comp) )
			return Idynomics.simulator.getCompartment( 
					Idynomics.simulator.getCompartmentNames().get( 
					Integer.valueOf(comp) ) );
		Log.out(this.getClass().getSimpleName() + " could not retrieve "
				+ "compartment: " + comp);
		return null;
		
	}
}
