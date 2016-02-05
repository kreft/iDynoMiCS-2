package agent.event.library;

import grid.SpatialGrid;
import agent.Agent;
import agent.event.Event;
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
	public void start(Agent agent, Agent compliant, Double timeStep)
	{
		SpatialGrid MassGrid = agent.getCompartment().getSolute(input[1]);
		MassGrid.addValueAt(ArrayType.CONCN, MassGrid.getCoords((double[]) 
				agent.get(input[2])), (double) agent.get(input[0]));
	}
	
	/**
	 * Events are general behavior patterns, copy returns this
	 */
	public Object copy() {
		return this;
	}
}
