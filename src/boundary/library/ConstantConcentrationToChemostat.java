/**
 * 
 */
package boundary.library;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * \brief set chemostat inflow at a constant concentration
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ConstantConcentrationToChemostat extends Boundary
{
	/**
	 * Solute concentrations.
	 */
	private Map<String,Double> _concns = new HashMap<String,Double>();

	public ConstantConcentrationToChemostat()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {

		this.setVolumeFlowRate( XmlHandler.obtainDouble ( 
				xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag() ) );
		
		NodeList childNodes = XmlHandler.getAll(xmlElement, XmlRef.solute);
		Element childElem;
		if ( childNodes != null )
		{
			for ( int i = 0; i < childNodes.getLength(); i++ )
			{
				childElem = (Element) childNodes.item(i);
				/* 
				 * Skip boundaries that are not direct children of the shape
				 * node (e.g. those wrapped in a dimension).
				 */
				if ( childElem.getParentNode() != xmlElement )
					continue;

				this.setConcentration( 
						childElem.getAttribute( XmlRef.nameAttribute ), 
						XmlHandler.gatherDouble( childElem, 
								XmlRef.concentration) );
			}
		}
	}
	
	@Override
	public Class<?> getPartnerClass()
	{
		return null;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	public void setConcentration(String name, double concn)
	{
		this._concns.put(name, concn);
	}
	
	/**
	 * getConcentration of solute
	 * 
	 * (set to public for unit testing).
	 * @param name
	 * @return
	 */
	public double getConcentration(String name)
	{
		if ( this._concns.containsKey(name) )
			return this._concns.get(name);
		return 0.0;
	}
	
	@Override
	public void updateMassFlowRates()
	{
		for ( String name : this._environment.getSoluteNames() )
		{
			this._massFlowRate.put(name, 
					this.getConcentration(name) * this._volumeFlowRate);
		}
	}
	

	@Override
	public void additionalPartnerUpdate() {}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

}
