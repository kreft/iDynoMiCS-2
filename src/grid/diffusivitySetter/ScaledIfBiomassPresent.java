package grid.diffusivitySetter;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import dataIO.XmlHandler;
import dataIO.XmlRef;
import grid.SpatialGrid;
import static grid.ArrayType.DIFFUSIVITY;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import shape.Shape;

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
	// TODO this is not currently used
	protected double _threshold;
	
	/**
	 * \brief Initiation from XML node.
	 * 
	 * @param xmlNode
	 */
	public void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;

		this._defaultDiffusivity = Double.valueOf(XmlHandler.obtainAttribute(
				elem, XmlRef.defaultDiffusivity));
		
		this._biofilmDiffusivity = Double.valueOf(XmlHandler.obtainAttribute(
				elem, XmlRef.biofilmDiffusivity));
		
		this._threshold = Double.valueOf(XmlHandler.obtainAttribute(elem, 
				XmlRef.threshold));
	}
	
	@Override
	public void updateDiffusivity(SpatialGrid diffusivityGrid,
			EnvironmentContainer env, AgentContainer agents)
	{
		/*
		 * Reset the diffusivity array.
		 */
		diffusivityGrid.newArray(DIFFUSIVITY, this._defaultDiffusivity);
		/*
		 * Iterate over the array, updating each voxel as it is visited.
		 */
		Shape shape = env.getShape();
		int nDim = shape.getNumberOfDimensions();
		double[] location = new double[nDim];
		double[] dimension = new double[nDim];
		int[] coord = shape.resetIterator();
		while ( shape.isIteratorValid() )
		{
			/* Find all agents that overlap with this voxel. */
			shape.voxelOriginTo(location, coord);
			shape.getVoxelSideLengthsTo(dimension, coord);
			List<Agent> neighbors = agents.treeSearch(location, dimension);
			/* If there are any agents in this voxel, update the diffusivity. */
			if ( ! neighbors.isEmpty() )
			{
				// TODO Calculate the total biomass/concentration, see if above
				// the threshold
				diffusivityGrid.setValueAt(
							DIFFUSIVITY, coord, this._biofilmDiffusivity);
			}
			/* Move onto the next voxel. */
			coord = shape.iteratorNext();
		}
	}
}
