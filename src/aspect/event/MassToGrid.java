package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import grid.ArrayType;
import grid.SpatialGrid;

/**
 * \brief Created for testing purposes: Event writes agent mass to SpatialGrid.
 * Similar to the method used in iDynoMiCS 1.
 * 
 * handy for follow pressure functionality which should improve biofilm
 * mechanical relaxation greatly
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 */
public class MassToGrid  extends Event
{
	public void start(AspectInterface initiator,
							AspectInterface compliant, Double timeStep)
	{
		Agent agent = (Agent) initiator;
		SpatialGrid massGrid = agent.getCompartment().getSolute("biomass");
		/*
		 * Write mass to target grid.
		 */
		massGrid.addValueAt(ArrayType.CONCN, 
				agent.getCompartment().getShape().getCoords(
				( (Body) agent.getValue("body") ).getPosition(0) ), 
				agent.getDouble("mass") );
	}
} 

