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
 * \brief set calculate transfer to/from membrame to a chemostat
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class MembraneToChemostat extends Boundary
{
	
	protected double _transferCoefficient;
	
	protected boolean _volumeSpecific;
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();

	public MembraneToChemostat()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {

		this._transferCoefficient =  XmlHandler.obtainDouble( 
				xmlElement, XmlRef.transferCoefficient, this.defaultXmlTag() );
		
		this._volumeSpecific = Boolean.valueOf( XmlHandler.obtainAttribute( 
				xmlElement, XmlRef.volumeSpecific, this.defaultXmlTag() ) );
		
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
						Double.valueOf( childElem.getAttribute( 
						XmlRef.concentration ) ) );
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
	
	private double getConcentration(String name)
	{
		if ( this._concns.containsKey(name) )
			return this._concns.get(name);
		return 0.0;
	}
	
	@Override
	public void updateMassFlowRates()
	{
		double vol = this._environment.getShape().getTotalRealVolume();
		for ( String name : this._concns.keySet() )
		{
			double deltaC = this.getConcentration(name) - 
					this._environment.getAverageConcentration(name);
			if (this._volumeSpecific)
				this._massFlowRate.put(name, this._transferCoefficient * deltaC * vol);
			else
				this._massFlowRate.put(name, this._transferCoefficient * deltaC);
		}
	}
	

	@Override
	public void additionalPartnerUpdate() {}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

}
