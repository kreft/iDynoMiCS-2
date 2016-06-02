package aspect.calculated;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Calculated;
import aspect.AspectRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, volumeDistribution
 */
public class VoxelDistributionMap extends Calculated
{
	
	public String MASS = AspectRef.agentMass;
	public String DISTRIBUTIONMAP = AspectRef.agentVolumeDistributionMap;
	
	/**
	 * input mass, volumeDistribution
	 */
	public VoxelDistributionMap()
	{
		setInput("mass,volumeDistribution");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		/*
		 * Obtain volume distribution map.
		 */
		@SuppressWarnings("unchecked")
		HashMap<int[],Double> distrib =
				(HashMap<int[],Double>) aspectOwner.getValue(DISTRIBUTIONMAP);
		/**
		 * calculate total volume
		 */
		double totalVol = 0.0;
		for( int[] key : distrib.keySet() )
			totalVol += distrib.get(key);
		/*
		 * Calculate hypothetical density.
		 */
		double mv =  aspectOwner.getDouble(MASS) / totalVol;
		
		/**
		 * assign appropriate mass portions to grid cells
		 */
		HashMap<int[],Double> massDistribution = new HashMap<int[],Double>();
		for(int[] key : distrib.keySet())
			massDistribution.put( key, distrib.get(key) * mv );
		return massDistribution;
	}
}
