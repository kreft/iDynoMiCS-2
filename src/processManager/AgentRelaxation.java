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
	
	// FIXME work in progress
	// set Mechanical stepper
	
	double dtMech;
	double vSquare;
	double tMech;		
	
	// TODO the following should folow from the protocol file
	double dtBase		= 0.01;	
	double maxMovement	= 0.1;
	String method 		= "euler";
	boolean timeLeap	= true;
	
	private void updateForces(EnvironmentContainer environment, AgentContainer agents) 
	{
		agents.refreshSpatialRegistry();
		//FIXME hard coded periodic boundaries and domain size for test case, initiate properly
		//TODO: in my opinion this information should all just come from the compartment
		Volume iterator = new Volume(agents.getShape());
		
		// Calculate forces
		for(Agent agent: agents.getAllLocatedAgents()) 
		{
			//agent.innerSprings();	// TODO method needs to be implemented (but not in Agent())
			for(Agent neighbour: agents._agentTree.cyclicsearch(
					(double[]) agent.get("#boundingLower"), /// TODO Add optional extra margin for pulls!!!
					(double[]) agent.get("#boundingSides"))) 
			{
				if (agent.identity() > neighbour.identity())
					{
					iterator.neighbourInteraction(			// TODO this needs to be expanded to also include other agent types
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
		int nstep	= 0;
		tMech		= 0.0;
		dtMech 		= 0.0005; // TODO (initial) time step.. needs to be build out of protocol file
		
		// if higher order ODE solvers are used we need additional space to write.
		switch (method)
		{
			case "heun" :
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get("body")).getPoints())
						point.setC(2);
				break;
		}
		
		// Mechanical relaxation
		while(tMech < _timeStepSize) 
		{	
			updateForces(environment, agents);
			
			/// obtain current highest particle velocity
			vSquare = 0.0;
			for(Agent agent: agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get("body")).getPoints())
					if ( Vector.normSquare(point.dxdt((double) agent.get("radius"))) > vSquare )
						vSquare = Vector.normSquare(point.dxdt((double) agent.get("radius")));
			
			// time Leaping set the time step to match a max traveling distance
			// divined by 'maxMovement', for a 'fast' run.
			if(timeLeap) 
				dtMech = maxMovement / (Math.sqrt(vSquare)*3.0+0.001);   // this 3.0 should match the fPush of Volume.. needs to be constructed out of protocol file.
			
			// prevent to relaxing longer than the global _timeStepSize
			if(dtMech > _timeStepSize-tMech)
				dtMech = _timeStepSize-tMech;
			
			// perform the step using (method)
			switch (method)
			{
				case "shove" :
					for(Agent agent: agents.getAllLocatedAgents())
						for (Point point: ((Body) agent.get("body")).getPoints())
							point.shove(dtMech, (double) agent.get("radius"));
					if (vSquare == 0.0) // continue until all overlap is resolved
						tMech = _timeStepSize;
				break;
			
				case "euler" :
					/// Euler's method
					for(Agent agent: agents.getAllLocatedAgents())
						for (Point point: ((Body) agent.get("body")).getPoints())
							point.euStep(dtMech, (double) agent.get("radius"));
					tMech += dtMech;
					break;
				
		// NOTE : higher order ODE solvers don't like time Leaping.. be careful.
				case "heun" :
					/// Heun's method
					for(Agent agent: agents.getAllLocatedAgents())
						for (Point point: ((Body) agent.get("body")).getPoints())
							point.heun1(dtMech, (double) agent.get("radius"));
					updateForces(environment,agents);
					for(Agent agent: agents.getAllLocatedAgents())
						for (Point point: ((Body) agent.get("body")).getPoints())
							point.heun2(dtMech, (double) agent.get("radius"));
					// Set time step
					tMech += dtMech;
					break;
			}

			for(Agent agent: agents.getAllLocatedAgents())
				for (Point point: ((Body) agent.get("body")).getPoints())
				{
					agents.getAgentBoundaries().forEach((k,v)->
					point.setPosition(v.inFrameLocation(point.getPosition())));
				}
			nstep++;
		}
		if(this._debugMode)
			System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
