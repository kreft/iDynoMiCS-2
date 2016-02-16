package grid.domainSetter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import dataIO.Log;
import dataIO.Log.tier;
import dataIO.XmlHandler;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;

/**
 * 
 * @author baco
 *
 */
public class DiffusivitySetter {

	protected double _defaultDiffusivity;
	
	protected double _biofilmDiffusivity;
	
	protected double _threshold;
	
	public void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;

		this._defaultDiffusivity = Double.valueOf(XmlHandler.obtainAttribute(
				elem, "defaultDiffusivity"));
		
		this._biofilmDiffusivity = Double.valueOf(XmlHandler.obtainAttribute(
				elem, "biofilmDiffusivity"));
		
		this._threshold = Double.valueOf(XmlHandler.obtainAttribute(elem, 
				"threshold"));
	}

	public void updateDiffusivity(SpatialGrid diffusivityGrid, SpatialGrid[] 
			concentrationGrids, EnvironmentContainer env, AgentContainer agents)
	{
		/*
		 * Reset the domain array.
		 */
		diffusivityGrid.newArray(ArrayType.DIFFUSIVITY);
		
		/**
		 * backup method if no grids to estimate the diffusivity grid is given
		 */
		if(concentrationGrids.length == 0)
		{
			SpatialGrid biomass = null;
			try
			{
				biomass = env.getSoluteGrid(NameRef.defaultBiomassGrid);
			}
			catch (IllegalArgumentException e)
			{
				Log.out(tier.CRITICAL, "unable to write diffusivity grid"
						+ "input grids");
			}
			
			/**
			 * NOTE: currently always updating biomass grid IF it is not given
			 * as input argument
			 */
			biomass.setAllTo(ArrayType.CONCN, 0.0);
			
			/**
			 * NOTE: agent must have a mass to grid method that writes to
			 */
			for ( Agent agent : agents.getAllAgents() )
			{
				agent.event("massToGrid");
			}
			concentrationGrids = new SpatialGrid[]{ biomass };
		}
		
		/**
		 * reset diffusivity grid
		 */
		diffusivityGrid.setAllTo(ArrayType.DIFFUSIVITY, _defaultDiffusivity);
				
		/*
		 * Iterate over all voxels, checking if there are agents nearby.
		 */
		int[] coords = diffusivityGrid.resetIterator();
		while ( diffusivityGrid.isIteratorValid() )
		{
			double sum = 0.0;
			for (SpatialGrid grid : concentrationGrids)
				sum += grid.getValueAt(ArrayType.CONCN, coords);
			if(sum > _threshold)
				diffusivityGrid.setValueAt(ArrayType.DIFFUSIVITY, coords, 
						_biofilmDiffusivity);
			coords = diffusivityGrid.iteratorNext();
		}
	}
}
