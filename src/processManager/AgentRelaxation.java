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
		
		double dtBase	= 0.01;
		double dtMech 	= 0.002; // initial time step
		double tMech	= 0.0;
		int nstep		= 0;
		double tStep	= _timeStepSize;
		double maxMovement		= 0.1; //maximum distance an object may travel per step usually defined by smallest object
		boolean shoving = false;
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
					for (Point point: ((Body) agent.get("body")).getPoints())
						if ( Vector.normSquare(point.dxdt((double) agent.get("radius"))) > vSquare )
							vSquare = Vector.normSquare(point.dxdt((double) agent.get("radius")));
				
				/// time step adjustment
				dtMech = maxMovement / Math.sqrt(vSquare)+0.001;
				if(dtMech > tStep-tMech)
					dtMech = tStep-tMech;
				
				/// Euler's method
//				for(Agent agent: agents.getAllLocatedAgents())
//					for (Point point: ((Body) agent.get("body")).getPoints())
//						point.euStep(dtMech, (double) agent.get("radius"));
				
				/// Heun's method
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get("body")).getPoints())
						point.heun1(dtMech, (double) agent.get("radius"));

				updateForces(agents);
				
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get("body")).getPoints())
						point.heun2(dtMech, (double) agent.get("radius"));

				
				// Set time step
				tMech += dtMech;
//				dtMech = dtBase - Math.min(dtBase * Math.sqrt(vSquare),0.92*dtBase);
//				dtMech = dtBase / (Math.sqrt(vSquare) + 0.01);
				// fineness of movement / (speed + stability factor)
				// stability factor of 0.02 seems to work fine, yet may change in
				// the future.
//				System.out.println("dt step: " + dtMech);
				
			}
			nstep++;
		}

		System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
