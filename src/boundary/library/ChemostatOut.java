/**
 * 
 */
package boundary.library;

import java.util.Iterator;

import org.w3c.dom.Element;

import boundary.Boundary;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * \brief The ChemostatOut boundary allows to set an outflow with a solute
 * concentration that equals the concentration in the chemostat.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ChemostatOut extends Boundary
{
	private boolean constantVolume = false;

	public ChemostatOut()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		if (! XmlHandler.hasAttribute(xmlElement, XmlRef.constantVolume))
			this.setVolumeFlowRate( Double.valueOf( XmlHandler.obtainAttribute( 
					xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag() ) ) );
		else
			this.constantVolume = true; 
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
		if (this.constantVolume)
		{
			double totalOutFlow = 0.0;
			Iterator<Boundary> otherBounds = this._environment.getNonSpatialBoundaries().iterator();
			while (otherBounds.hasNext())
			{
				Boundary bound = otherBounds.next();
				if (bound != this)
					totalOutFlow -= bound.getVolumeFlowRate();
			}
			this.setVolumeFlowRate(totalOutFlow);
		}
		return this._environment.getAverageConcentration(name) * this._volumeFlowRate;

	}
	
	@Override
	public void updateMassFlowRates()
	{
		if (this.constantVolume)
		{
			double totalOutFlow = 0.0;
			Iterator<Boundary> otherBounds = this._environment.getShape().getAllBoundaries().iterator();
			while (otherBounds.hasNext())
			{
				Boundary bound = otherBounds.next();
				if (bound != this)
					totalOutFlow -= bound.getVolumeFlowRate();
			}
			this.setVolumeFlowRate(totalOutFlow);
		}
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
