package agent.state.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Calculated;

public class VoxelDistributionMap extends Calculated {
	
	/**
	 * input mass, volumeDistribution
	 */
	public VoxelDistributionMap()
	{
		setInput("mass,volumeDistribution");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		/**
		 * obtain volume distribution map
		 */
		@SuppressWarnings("unchecked")
		HashMap<int[],Double> distrib = (HashMap<int[],Double>) aspectOwner.getValue(input[1]);
		double totalVol = 0.0;
		
		/**
		 * calculate total volume
		 */
		for(int[] key : distrib.keySet())
			totalVol = distrib.get(key);
		
		/**
		 * calculate hypothetical density
		 */
		double mv =  aspectOwner.getDouble(input[0]) / totalVol;
		
		/**
		 * assign appropriate mass portions to grid cells
		 */
		HashMap<int[],Double> massDistribution = new HashMap<int[],Double>();
		for(int[] key : distrib.keySet())
			massDistribution.put(key, distrib.get(key) * mv);
		
		return massDistribution;
	}
	
}
