package processManager;

import linearAlgebra.Vector;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import agent.Agent;
import agent.body.*;

	////////////////////////
	// WORK IN PROGRESS, initial version
	////////////////////////

public class AgentRelaxation extends ProcessManager {
	
	private void updateForces(AgentContainer agents) 
	{
		agents.refreshSpatialRegistry();
		
		
		Volume iterator = new Volume(agents.getNumDims());
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
	}

	
	protected void internalStep(EnvironmentContainer environment,
											AgentContainer agents) {
		// FIXME work in progress
		// Reset Mechanical stepper

		double dtMech 	= 0.0001; // initial time step
		double tMech	= 0.0;
		int nstep		= 0;
		double tStep	= _timeStepSize;
		double maxMovement		= 0.009; //maximum distance an object may travel per step usually defined by smallest object
		boolean shoving = true;
		// Mechanical relaxation
		while(tMech < tStep) 
		{			
			double vSquare = 0.0;
			updateForces(agents);
			
			if (shoving)
			{
				for(Agent agent: agents.getAllLocatedAgents())
				{
					for (Point point: ((Body) agent.get("body")).getPoints())
					{
						point.shove(dtMech, (double) agent.get("radius"));
						// FIXME quick 'n dirty
						if ( Vector.normSquare(point.getVelocity()) > vSquare )
							vSquare = Vector.normSquare(point.getVelocity());
					}
				}
				if (vSquare == 0.0)
					tMech = tStep;
			}
			else
			{
				
				for(Agent agent: agents.getAllLocatedAgents())
				{
					for (Point point: ((Body) agent.get("body")).getPoints())
					{
						point.euStep(dtMech, (double) agent.get("radius"));
						if ( Vector.normSquare(point.getVelocity()) > vSquare )
							vSquare = Vector.normSquare(point.getVelocity());
					}
				}
				
				// Set time step
				tMech += dtMech;
				dtMech = maxMovement / (Math.sqrt(vSquare)+0.01);
				// fineness of movement / (speed + stability factor)
				// stability factor of 0.02 seems to work fine, yet may change in
				// the future.
				if(dtMech > tStep-tMech)
					dtMech = tStep-tMech;
			}
			nstep++;
		}

		System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
