/**
 * 
 */
package aspect.calculated;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.ObjectFactory;
import grid.ArrayType;
import grid.SpatialGrid;
import referenceLibrary.AspectRef;
import shape.Shape;
import shape.subvoxel.CoordinateMap;

/**
 * \brief detect local solute concentrations and return as a Hashmap
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class LocalConcentrations extends Calculated
{
	
	private String VD_TAG = AspectRef.agentVolumeDistributionMap;
	
	public Object get(AspectInterface aspectOwner)
	{
		HashMap<String,Double> out = new HashMap<String,Double>();
		Tier level = Tier.NORMAL;

		Agent anAgent = (Agent) aspectOwner;
		Compartment comp = anAgent.getCompartment();

		/*
		 * Dimensionless compartment (chemostat)
		 */
		if (comp.isDimensionless())
		{
			return ObjectFactory.copy(
					comp.environment.getAverageConcentrations() );
		}
		
		/* 
		 * Dimensional compartment
		 */
		Collection<SpatialGrid> solutes = comp.environment.getSolutes();

		CoordinateMap distribMap;
		if ( ! anAgent.isAspect(VD_TAG) )
		{
			distribMap = new CoordinateMap();
			Shape shape = anAgent.getCompartment().getShape();
			int[] coords = shape.getCoords( ((Body) anAgent.get(AspectRef.agentBody)).getCenter(shape));
			distribMap.put(coords,1.0);
		} else
		{
			Map<Shape, CoordinateMap> mapOfMaps = (Map<Shape, CoordinateMap>) anAgent.getValue(VD_TAG);
			distribMap = mapOfMaps.get(comp.getShape());
		}
		
		/*
		 * Loop over the coordinates, storing the solute concentrations.
		 */
		for ( SpatialGrid solute: solutes )
		{
			double concn = 0;
			for ( int[] coord : distribMap.keySet() )
			{
				 concn += solute.getValueAt(ArrayType.CONCN, coord);
			}
			/* store averaged local concentration, assuming equal distribution
			 * for a more correct implementation consider Shape getVoxelVolume 
			 */
			out.put(solute.getName(), concn / distribMap.keySet().size() );
		}
		return out;
	}
}