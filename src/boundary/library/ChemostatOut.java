/**
 * 
 */
package boundary.library;

import org.w3c.dom.Element;

import boundary.Boundary;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * \brief set outflow
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ChemostatOut extends Boundary
{
	public ChemostatOut()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {

		this.setVolumeFlowRate( Double.valueOf( XmlHandler.obtainAttribute( 
				xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag() ) ) );
	}
	
	@Override
	protected Class<?> getPartnerClass()
	{
		return null;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	
	private double getConcentration(String name)
	{
		return this._environment.getAverageConcentration(name);
	}
	
	@Override
	public double getMassFlowRate(String name)
	{
		return this._environment.getAverageConcentration(name) * this._volumeFlowRate;

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
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

}
