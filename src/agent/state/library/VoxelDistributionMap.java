package agent.state.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Calculated;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, volumeDistribution
 */
// TODO this class may now be redundant: see grid.subgrid.CoordinateMap
public class VoxelDistributionMap extends Calculated
{
	public Object get(AspectInterface aspectOwner)
	{
		/*
		 * Obtain volume distribution map.
		 */
		@SuppressWarnings("unchecked")
		HashMap<int[],Double> distrib = 
						(HashMap<int[],Double>) aspectOwner.getValue(input[1]);
		/*
		 * Calculate total volume.
		 */
		double totalVol = 0.0;
		for(int[] key : distrib.keySet())
			totalVol += distrib.get(key);
		/*
		 * Calculate hypothetical density.
		 */
		double mv =  aspectOwner.getDouble(input[0]) / totalVol;
		/*
		 * Assign appropriate mass portions to grid cells.
		 */
		HashMap<int[],Double> massDistribution = new HashMap<int[],Double>();
		for(int[] key : distrib.keySet())
			massDistribution.put(key, distrib.get(key) * mv);
		return massDistribution;
	}
}
