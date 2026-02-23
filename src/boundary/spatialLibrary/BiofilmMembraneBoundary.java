package boundary.spatialLibrary;

import boundary.ConcentrationBoundry;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.XmlHandler;
import grid.SpatialGrid;
import instantiable.Instantiable;
import org.w3c.dom.Element;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class BiofilmMembraneBoundary extends SpatialBoundary implements Instantiable, ConcentrationBoundry
{
    /**
     * Solute concentrations.
     */
    protected Map<String,Double> _concns = new HashMap<String,Double>();

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/

	public BiofilmMembraneBoundary()
	{ 
		super(); 
	}
	
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

    /**
     * TODO update
     * @return
     */
	@Override
	public Module getModule()
	{
		Module modelNode = super.getModule();

		for ( String sol : this._concns.keySet() ) {
			Module soluteNode = new Module(XmlRef.solute, this);
			soluteNode.setRequirements(Module.Requirements.ZERO_TO_MANY);

			soluteNode.add(new Attribute(XmlRef.nameAttribute,
					sol, null, true));

			soluteNode.add(new Attribute(XmlRef.concentration,
					String.valueOf( this._concns.get(sol) ), null, true));
			modelNode.add(soluteNode);
		}
		return modelNode;
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

    @Override
    public boolean soluteFlux(String soluteName) {
        if(this._concns.containsKey(soluteName))
            return true;
        else
            return false;
    }

    @Override
    protected boolean needsLayerThickness()
    {
        return false;
    }

    @Override
    protected double calcDiffusiveFlow(SpatialGrid grid)
    {
        /*
         * TODO: update for combination with chemostat
         */
        return 0.0;
    }
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

    public double getConcentration(String soluteName)
    {
        try
        {
            return this._concns.get(soluteName);
        }
        catch (Exception e)
        {
            return 0.0;
        }
    }

    @Override
    public void updateWellMixedArray()
    {

    }

	@Override
	public void additionalPartnerUpdate() {}

	public boolean retainsAgents()
	{
		return true;
	}
}
