/**
 * 
 */
package aspect.calculated;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AgentEnvironmentInteraction;
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

	public Object get(AspectInterface aspectOwner)
	{
		return AgentEnvironmentInteraction.getLocalConcentrations(aspectOwner);
	}
}