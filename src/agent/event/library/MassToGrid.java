package agent.event.library;

import grid.SpatialGrid;
import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import grid.SpatialGrid.ArrayType;

/**
 * Created for testing purposes, Event writes agent mass to SpatialGridd,
 * Simular to the method used in iDynoMiCS 1
 * @author baco
 *
 * NOTE: input "mass","targetGrid","CoccoidCenter"
 */
public class MassToGrid  extends Event {

	/**
	 * write mass to target grid.
	 */
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		SpatialGrid MassGrid = agent.getCompartment().getSolute(input[1]);
		MassGrid.addValueAt(ArrayType.CONCN, MassGrid.getCoords((double[]) 
				agent.get(input[2])), (double) agent.get(input[0]));
	}
}
