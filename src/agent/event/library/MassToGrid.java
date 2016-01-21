package agent.event.library;

import grid.SpatialGrid;
import agent.Agent;
import agent.event.Event;
import grid.SpatialGrid.ArrayType;
	
public class MassToGrid  extends Event {

	// input "mass","targetGrid","CoccoidCenter"

	public void start(Agent agent, Agent compliant, Double timeStep)
	{
		SpatialGrid MassGrid = agent.getCompartment().getSolute(input[1]);
		MassGrid.addValueAt(ArrayType.CONCN, MassGrid.getCoords((double[]) agent.get(input[2])), (double) agent.get(input[0]));
	}
	
	public Object copy() {
		return this;
	}
}
