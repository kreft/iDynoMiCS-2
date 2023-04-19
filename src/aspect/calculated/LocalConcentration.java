/**
 * 
 */
package aspect.calculated;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import compartment.Compartment;
import dataIO.Log.Tier;
import dataIO.ObjectFactory;
import expression.Expression;
import grid.ArrayType;
import grid.SpatialGrid;
import referenceLibrary.AspectRef;
import shape.Shape;
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.IntegerArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * \brief detect local solute concentrations and return as a Hashmap
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class LocalConcentration extends Calculated
{
	
	private String VD_TAG = AspectRef.agentVolumeDistributionMap;

	//FIXME input is not saved to xml!
	@Override
	public void setInput(String input)
	{
		if( input == null)
			return;
		// or value?
		this._input = input.replaceAll("\\s+","");
	}

	public Object get(AspectInterface aspectOwner)
	{
		Double out = 0.0;
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
		SpatialGrid solute = comp.environment.getSoluteGrid(this._input);

		HashMap<IntegerArray,Double> distribMap = new HashMap<IntegerArray,Double>();
		Shape shape = anAgent.getCompartment().getShape();
		Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps = new HashMap<Shape, HashMap<IntegerArray,Double>>(2, 1.0f);
		
		if ( ! anAgent.isAspect(VD_TAG) )
		{
			IntegerArray coordArray = new IntegerArray(
					shape.getCoords(shape.getVerifiedLocation(((Body) anAgent.get(AspectRef.agentBody)).getCenter(shape))));
			distribMap.put(coordArray, 1.0);
		} else
		{
			mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>) anAgent.getValue(VD_TAG);
			distribMap = mapOfMaps.get( anAgent.getCompartment().getShape() );
		}
		

		double concn = 0;
		for ( IntegerArray coord : distribMap.keySet() )
		{
			 concn += solute.getValueAt(ArrayType.CONCN, coord.get());
		}
		/* store averaged local concentration, assuming equal distribution
		 * for a more correct implementation consider Shape getVoxelVolume
		 */
		out = concn / distribMap.keySet().size();

		return out;
	}
}