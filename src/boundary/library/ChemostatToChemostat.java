package boundary.library;

import org.w3c.dom.Element;

import boundary.Boundary;
import compartment.Compartment;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import utility.Helper;

/**
 * \brief Connective boundary linking one dimensionless compartment to another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToChemostat extends Boundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 */

	public ChemostatToChemostat()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		super.instantiate(xmlElement, parent);
		this._dominant = Boolean.parseBoolean(
				XmlHandler.gatherAttribute(xmlElement,
				XmlRef.dominant));
		if (! XmlHandler.hasAttribute(xmlElement, XmlRef.constantVolume))
			this.setVolumeFlowRate( XmlHandler.obtainDouble( 
					xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag()));
		this._soluteRetention = Helper.setIfNone( XmlHandler.gatherBoolean(
				xmlElement, XmlRef.soluteRetention ), false);
	}

	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return ChemostatToChemostat.class;
	}

	@Override
	public Boundary makePartnerBoundary()
	{
		ChemostatToChemostat cIn = 
				(ChemostatToChemostat) super.makePartnerBoundary();
		cIn.setVolumeFlowRate( - this._volumeFlowRate);
		return cIn;
	}

	/**
	 * \brief Make a partner boundary and set it to the compartment given.
	 * 
	 * @param comp
	 */
	// TODO consider deletion
	public void setPartnerCompartment(Compartment comp)
	{
		Boundary cIn = this.makePartnerBoundary();
		comp.addBoundary(cIn);
	}
	
	@Override
	public Module getModule()
	{
		Module mod = super.getModule();
		if( this._dominant)
			mod.add( new Attribute( XmlRef.dominant, String.valueOf( 
					this._dominant ), null, true ));
		return mod;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void additionalPartnerUpdate() {}
}
