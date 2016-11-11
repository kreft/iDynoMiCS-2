package boundary.spatialLibrary;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import generalInterfaces.Instantiatable;
import grid.SpatialGrid;
import nodeFactory.NodeConstructor;
import referenceLibrary.XmlRef;
import shape.Dimension.DimName;

/**
 * \brief Spatial boundary where solute concentrations are kept fixed. Solid
 * surface to agents. Intended for testing purposes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class FixedBoundary extends SpatialBoundary implements Instantiatable
{
	/**
	 * Solute concentrations.
	 */
	protected Map<String,Double> _concns = new HashMap<String,Double>();
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * FIXME essential for instantiation
	 */
	public FixedBoundary()
	{
		super();
	}
	
	/**
	 * FIXME essential for instantiation
	 */
	public void init(Element xmlElement, NodeConstructor parent)
	{
		super.init(xmlElement,parent);
		this._detachability = 0.0;
		
		for ( Element e : XmlHandler.getElements(xmlElement, XmlRef.concentration))
		{
			setConcentration(XmlHandler.obtainAttribute(e, XmlRef.nameAttribute,
					XmlRef.concentration), Double.valueOf(XmlHandler.obtainAttribute(e, 
					XmlRef.valueAttribute, XmlRef.concentration)));
		}
	}
	
	/**
	 * \brief Construct a fixed boundary by giving it the information it
	 * needs about its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public FixedBoundary(DimName dim, int extreme)
	{
		super(dim, extreme);
		this._detachability = 0.0;
	}
	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	protected Class<?> getPartnerClass()
	{
		/* 
		 * This boundary shouldn't really have a partner, but if one is
		 * requested then just return another fixed boundary.
		 */
		return FixedBoundary.class;
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
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/
	
	@Override
	protected double getDetachability()
	{
		return 0.0;
	}
	
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
