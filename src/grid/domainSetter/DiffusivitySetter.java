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
 * Simple diffusivity setter based allowing for two discrete diffusivity regimes
 * _defaultDiffusivity for the suspension, _biofilmDiffusivity for the biofilm
 * _threshold indicates the minimum concentration of biomass in a grid cell
 * before a grid cell is considered part of a biofilm rather than suspension
 * (set to 0.0 for iDynoMiCS one compatibility).
 * @author baco
 *
 */
public class DiffusivitySetter {

	/**
	 * Diffusivity for the suspension
	 */
	protected double _defaultDiffusivity;
	
	/**
	 * Diffusivity for the biofilm
	 */
	protected double _biofilmDiffusivity;
	
	/**
	 * indicates the minimum concentration of biomass in a grid cell
	 * before a grid cell is considered part of a biofilm rather than suspension
	 * (set to 0.0 for iDynoMiCS one compatibility).
	 */
	protected double _threshold;
	
	/**
	 * initiation from xml node
	 * @param xmlNode
	 */
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

	/**
	 * base diffusivity on given concentration grids, use and update biomassgrid
	 * if no input concentration grids are given.
	 * @param diffusivityGrid
	 * @param concentrationGrids
	 * @param env
	 * @param agents
	 */
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
