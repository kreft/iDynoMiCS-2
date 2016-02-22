package grid.diffusivitySetter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import dataIO.Log;
import dataIO.Log.tier;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;

/**
 * \brief Diffusivity setter that has two discrete diffusivity regimes: where
 * there is little of no biomass present, and where there is a lot of biomass.
 * 
 * <p>
 * _defaultDiffusivity for the suspension, _biofilmDiffusivity for the biofilm
 * _threshold indicates the minimum concentration of biomass in a grid cell
 * before a grid cell is considered part of a biofilm rather than suspension
 * (set to 0.0 for iDynoMiCS one compatibility).
 *  </p>
 *  
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.

 */
public class ScaledIfBiomassPresent implements IsDiffusivitySetter
{
	/**
	 * Diffusivity for the suspension.
	 */
	protected double _defaultDiffusivity;
	
	/**
	 * Diffusivity for the biofilm.
	 */
	protected double _biofilmDiffusivity;
	
	/**
	 * Indicates the minimum concentration of biomass in a grid cell before a
	 * grid cell is considered part of a biofilm rather than suspension
	 * (set to 0.0 for compatibility with iDynoMiCS 1).
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
				elem, XmlLabel.defaultDiffusivity));
		
		this._biofilmDiffusivity = Double.valueOf(XmlHandler.obtainAttribute(
				elem, XmlLabel.biofilmDiffusivity));
		
		this._threshold = Double.valueOf(XmlHandler.obtainAttribute(elem, 
				XmlLabel.threshold));
	}
	
	/**
	 * Base diffusivity on given concentration grids, use and update biomassgrid
	 * if no input concentration grids are given.
	 * 
	 * @param diffusivityGrid
	 * @param concentrationGrids
	 * @param env
	 * @param agents
	 */
	public void updateDiffusivity(SpatialGrid diffusivityGrid, SpatialGrid[] 
			concentrationGrids, EnvironmentContainer env, AgentContainer agents)
	{
		/*
		 * Reset the diffusivity array.
		 */
		diffusivityGrid.newArray(ArrayType.DIFFUSIVITY,
												this._defaultDiffusivity);
		/*
		 * Backup method if no grids to estimate the diffusivity grid is given
		 */
		if ( concentrationGrids.length == 0 )
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
			/*
			 * NOTE: currently always updating biomass grid IF it is not given
			 * as input argument
			 */
			biomass.setAllTo(ArrayType.CONCN, 0.0);
			/*
			 * NOTE: agent must have a mass to grid method that writes to
			 */
			for ( Agent agent : agents.getAllAgents() )
			{
				agent.event("massToGrid");
			}
			concentrationGrids = new SpatialGrid[]{ biomass };
		}
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
						this._biofilmDiffusivity);
			coords = diffusivityGrid.iteratorNext();
		}
	}
}
