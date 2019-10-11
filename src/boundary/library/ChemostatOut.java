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
import utility.Helper;

/**
 * \brief The ChemostatOut boundary allows to set an outflow with a solute
 * concentration that equals the concentration in the chemostat.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Sankalp Arya (sankalp.arya@nottingham.ac.uk), University of Nottingham, U.K.
 */
public class ChemostatOut extends ChemostatBoundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 */
	protected boolean constantVolume = false;

	public ChemostatOut()
	{
		super();
	}

	/**
	 * instantiate, load settings from xml
	 * 
	 * @param xmlElement
	 * @param parent
	 */
	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		if (! XmlHandler.hasAttribute(xmlElement, XmlRef.constantVolume))
			this.setVolumeFlowRate( XmlHandler.obtainDouble( 
					xmlElement, XmlRef.volumeFlowRate, this.defaultXmlTag()));
		else
			this.constantVolume = true;
		this._agentRemoval = Helper.setIfNone( Boolean.valueOf( 
				XmlHandler.gatherAttribute( xmlElement, XmlRef.agentRemoval ) ), 
				false);
	}
	
	/**
	 * Chemostat out has no partner classes
	 * @return
	 */
	@Override
	public Class<?> getPartnerClass()
	{
		return null;
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	/**
	 * NOTE: Using this method degrades any solver using it to doing euler steps
	 * on the solute transfer over the boundaries.
	 */
	@Override
	@Deprecated
	public double getMassFlowRate(String name)
	{
		if (this.constantVolume)
		{
			double totalOutFlow = 0.0;
			Iterator<Boundary> otherBounds = 
					this._environment.getNonSpatialBoundaries().iterator();
			while (otherBounds.hasNext())
			{
				Boundary bound = otherBounds.next();
				if (bound != this)
					totalOutFlow -= bound.getVolumeFlowRate();
			}
			this.setVolumeFlowRate(totalOutFlow);
		}
		return this._environment.getAverageConcentration(name) * 
				this.getVolumeFlowRate();

	}
	
	/**
	 * NOTE: Using this method degrades any solver using it to doing euler steps
	 * on the solute transfer over the boundaries.
	 */
	@Override
	@Deprecated
	public void updateMassFlowRates()
	{
		if (this.constantVolume)
		{
			double totalOutFlow = 0.0;
			Iterator<Boundary> otherBounds = 
					this._environment.getShape().getAllBoundaries().iterator();
			while (otherBounds.hasNext())
			{
				Boundary bound = otherBounds.next();
				if (bound != this)
					totalOutFlow -= bound.getVolumeFlowRate();
			}
			this.setVolumeFlowRate(totalOutFlow);
		}
		for ( String name : this._environment.getSoluteNames() )
			this.setMassFlowRate(name, this.getMassFlowRate(name));
	}
	
	@Override
	public void additionalPartnerUpdate() {}

}
