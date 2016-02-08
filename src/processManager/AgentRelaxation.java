package processManager;

import surface.Collision;
import surface.Link;
import surface.Point;
import surface.Surface;
import linearAlgebra.Vector;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

import java.util.List;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.Log.tier;

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
	String method 		= "shove";
	boolean timeLeap	= true;
	
	private void updateForces(EnvironmentContainer environment, AgentContainer agents) 
	{
		for(Agent agent: agents.getAllLocatedAgents()) 
			agent.event("updateBody");
		
		agents.refreshSpatialRegistry();
		//FIXME hard coded periodic boundaries and domain size for test case, initiate properly
		//TODO: in my opinion this information should all just come from the compartment
		Collision iterator = new Collision(null, agents.getShape());
		
		// Calculate forces
		for(Agent agent: agents.getAllLocatedAgents()) 
		{
			List<Link> links = ((Body) agent.get("body"))._links;
			for (int i = 0; i < links.size(); i++)
			{
				if (links.get(i).evaluate(iterator))
				{
					Log.out(tier.BULK, "Fillial link breakage due to "
							+ "over extending maximum link length.");
					links.remove(i);
				}
			}
			
			//agent.innerSprings();	// TODO method needs to be implemented (but not in Agent())
			for(Agent neighbour: agents._agentTree.cyclicsearch(
					(double[]) agent.get("#boundingLower"), /// TODO Add optional extra margin for pulls!!!
					(double[]) agent.get("#boundingSides"))) 
			{
				if (agent.identity() > neighbour.identity())
					{
					iterator.collision((Surface) agent.get("surface"), (Surface) neighbour.get("surface"));
					}
			}
			
			/*
			 * Boundary collisions
			 */
			for(Surface s : agents.getShape().getSurfaces())
			{
				iterator.collision(s, (Surface) agent.get("surface"));
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
					agents.getShape().applyBoundaries(point.getPosition());
				}
			nstep++;
		}
		if(this._debugMode)
			System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
