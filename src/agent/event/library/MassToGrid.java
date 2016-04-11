package agent.event.library;

import grid.SpatialGrid;
import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import grid.SpatialGrid.ArrayType;

/**
 * \brief Created for testing purposes: Event writes agent mass to SpatialGrid.
 * Similar to the method used in iDynoMiCS 1.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * NOTE: input "mass","targetGrid","CoccoidCenter"
 */
public class MassToGrid  extends Event
{
	public void start(AspectInterface initiator,
							AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		SpatialGrid massGrid = agent.getCompartment().getSolute(input[1]);
		/*
		 * Write mass to target grid.
		 */
		massGrid.addValueAt(ArrayType.CONCN, massGrid.getCoords((double[]) 
				agent.get(input[2])), (double) agent.get(input[0]));
	}
}
