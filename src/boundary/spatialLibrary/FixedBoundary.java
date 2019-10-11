package boundary.spatialLibrary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import grid.SpatialGrid;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * \brief Spatial boundary where solute concentrations are kept fixed. Solid
 * surface to agents. Intended for testing purposes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class FixedBoundary extends SpatialBoundary implements Instantiable
{
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public FixedBoundary()
	{ }
	
	public void instantiate(Element xmlElement, Settable parent)
	{
		super.instantiate(xmlElement,parent);
		
		Collection<Element> elements = 
				XmlHandler.getElements(xmlElement, XmlRef.solute);
		String name, concn;
		
		for ( Element e : elements )
		{
			name = XmlHandler.obtainAttribute(e,
					XmlRef.nameAttribute, XmlRef.concentration);
			this.setConcentration(name, XmlHandler.obtainDouble(e, 
					XmlRef.concentration, XmlRef.concentration));
		}
	}
	

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	@Override
	protected boolean needsLayerThickness()
	{
		return false;
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return null;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	/**
	 * \brief Set the concentration of a solute at this boundary.
	 * 
	 * @param name Name of the solute.
	 * @param concn Concentration of the solute.
	 */
	public void setConcentration(String name, double concn)
	{
		this._concns.put(name, concn);
	}
	
	@Override
	protected double calcDiffusiveFlow(SpatialGrid grid)
	{
		Tier level = Tier.DEBUG;
		Double concn = this._concns.get(grid.getName());
		if (concn == null)
		{	if (Log.shouldWrite(level))
				Log.out(level, "WARNING: unset fixed boundary concn");
			return 0.0;
		}
		return this.calcDiffusiveFlowFixed(grid, concn);
	}
	
	@Override
	public void updateWellMixedArray()
	{
		this.setWellMixedByDistance();
	}

	@Override
	public void additionalPartnerUpdate() {}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/
	
	@Override
	public void agentsArrive()
	{
		if ( ! this._arrivalsLounge.isEmpty() )
		{
			Log.out(Tier.NORMAL,
					"Unexpected: agents arriving at a fixed boundary!");
		}
		this.placeAgentsRandom();
		this.clearArrivalsLounge();
	}
}
