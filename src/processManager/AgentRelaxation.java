package processManager;

import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

import java.util.Collection;

import agent.Agent;
import agent.body.*;
import boundary.Boundary;

	////////////////////////
	// WORK IN PROGRESS, initial version
	////////////////////////

public class AgentRelaxation extends ProcessManager {

	
	protected void internalStep(EnvironmentContainer environment,
											AgentContainer agents) {
		// FIXME work in progress
		// Reset Mechanical stepper

		double dtMech 	= 0.0001; // initial time step
		double tMech	= 0.0;
		int nstep		= 0;
		double tStep	= _timeStepSize;
		double maxMovement		= 0.001; //maximum distance an object may travel per step usually defined by smallest object
		// Mechanical relaxation
		while(tMech < tStep) 
		{			
			agents.refreshSpatialRegistry();
			double vSquare = 0.0;
			
			Volume iterator = new Volume();
			// Calculate forces
			for(Agent agent: agents.getAllLocatedAgents()) 
			{
				//agent.innerSprings();
				for(Agent neighbour: agents._agentTree.search(
						(float[]) agent.get("lowerBoundingBox"), /// Add extra margin for pulls!!!
						(float[]) agent.get("dimensionsBoundingBox"))) 
				{
					if (agent.identity() > neighbour.identity())
						{
						iterator.neighbourInteraction(
								((Body) neighbour.get("body")).getPoints().get(0),
								((Body) agent.get("body")).getPoints().get(0) , 
								(double) agent.get("radius") + 
								(double) neighbour.get("radius"));
						}
				}
			}
			
			// Update velocity and position
			for(Agent agent: agents.getAllLocatedAgents())
			{
				for (Point point: ((Body) agent.get("body")).getPoints())
					vSquare = point.euStep(vSquare, dtMech, 
							(double) agent.get("radius"));
			}
			// Set time step
			tMech += dtMech;
			dtMech = maxMovement / (Math.sqrt(vSquare)+0.02);
			// fineness of movement / (speed + stability factor)
			// stability factor of 0.02 seems to work fine, yet may change in
			// the future.
			if(dtMech > tStep-tMech)
				dtMech = tStep-tMech;
			nstep++;
		}

		System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
