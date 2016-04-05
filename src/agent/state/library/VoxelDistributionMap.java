package agent.state.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.NameRef;

public class VoxelDistributionMap extends Calculated {
	
	public String MASS = NameRef.agentMass;
	public String DISTRIBUTIONMAP = NameRef.agentVolumeDistributionMap;
	
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
		HashMap<int[],Double> distrib = (HashMap<int[],Double>) aspectOwner.getValue(DISTRIBUTIONMAP);
		double totalVol = 0.0;
		
		/**
		 * calculate total volume
		 */
		for(int[] key : distrib.keySet())
			totalVol = distrib.get(key);
		
		/**
		 * calculate hypothetical density
		 */
		double mv =  aspectOwner.getDouble(MASS) / totalVol;
		
		/**
		 * assign appropriate mass portions to grid cells
		 */
		HashMap<int[],Double> massDistribution = new HashMap<int[],Double>();
		for(int[] key : distrib.keySet())
			massDistribution.put(key, distrib.get(key) * mv);
		
		return massDistribution;
	}
	
}
