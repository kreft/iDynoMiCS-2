package processManager;

import java.util.concurrent.ForkJoinPool;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import surface.Collision;
import surface.Point;
import surface.Surface;
import utility.Helper;
import dataIO.Log;
import dataIO.Log.Tier;


	////////////////////////
	// WORK IN PROGRESS, initial version
	////////////////////////

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentRelaxation extends ProcessManager
{
	/**
	 * TODO
	 */
	private static ForkJoinPool pool = new ForkJoinPool(4);
	
	/**
	 * TODO
	 */
	private boolean concurrent = false;
	
	/**
	 * Available relaxation methods
	 * @author baco
	 *
	 */
	private enum method
	{
		SHOVE,
		EULER,
		HEUN
	}
	
	// FIXME work in progress
	// set Mechanical stepper
	
	double dtMech;
	double vSquare;
	double tMech;		
	
	/**
	 * Relaxation parameters (overwritten by init)
	 */
	double dtBase		= 0.01;	
	double maxMovement	= 0.1;	
	method _method		= method.EULER;
	boolean timeLeap	= true;
	
	@Override
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
		/**
		 * Obtaining relaxation parameters
		 */
		dtBase		= Helper.setIfNone(getDouble("dtBase"),0.01);	
		maxMovement	= Helper.setIfNone(getDouble("maxMovement"),0.1);	
		_method		= method.valueOf(Helper.obtainInput(getString(
				"relaxationMethod"), "agent relaxation misses relaxation method"
				+ " (SHOVE,EULER,HEUN)"));
		timeLeap	= true;
	}
	
	/**
	 * Update forces on all agent mass points
	 * @param environment
	 * @param agents
	 */
	private void updateForces(EnvironmentContainer environment, 
			AgentContainer agents) 
	{
		/**
		 * Update agent body now required
		 */
		for(Agent agent: agents.getAllLocatedAgents()) 
			agent.event(NameRef.bodyUpdate);
		
		/**
		 * Updated bodies thus update spatial tree
		 */
		agents.refreshSpatialRegistry();
		if(concurrent)
			Log.out(Tier.DEBUG, "concurent agent relax currently dissabled");
//			pool.invoke(new ParWorker(agents));
		else
		{
			Collision iterator = new Collision(null, agents.getShape());
			
			// Calculate forces
			for(Agent agent: agents.getAllLocatedAgents()) 
			{
//				List<Link> links = ((Body) agent.get(NameRef.agentBody))._links;
//				for (int i = 0; i < links.size(); i++)
//				{
//					if (links.get(i).evaluate(iterator))
//					{
//						Log.out(tier.BULK, "Fillial link breakage due to "
//								+ "over extending maximum link length.");
//						links.remove(i);
//					}
//				}
				
				/**
				 * NOTE: currently missing internal springs for rod cells.
				 */
				
				double pullDist = (agent.isAspect(NameRef.agentPulldistance) ?
						agent.getDouble(NameRef.agentPulldistance) :
						0.0);
				
				/**
				 * perform neighborhood search and perform collision detection and
				 * response FIXME: this has not been adapted to multi surface
				 * objects!
				 * TODO Add optional extra margin for pulls!!!
				 */
				for(Agent neighbour: agents.treeSearch(

						((Body) agent.get(NameRef.agentBody)).getBoxes(
								pullDist)))
				{
					if (agent.identity() > neighbour.identity())
					{
						iterator.collision((Surface) agent.get("surface"), 
								(Surface) neighbour.get("surface"), pullDist +
								(neighbour.isAspect(NameRef.agentPulldistance) ?
								neighbour.getDouble(NameRef.agentPulldistance) :
								0.0));
					}
				}
				
				/*
				 * Boundary collisions
				 */
				for(Surface s : agents.getShape().getSurfaces())
				{
					iterator.collision(s, (Surface) agent.get("surface"), 0.0);
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
		switch (_method)
		{
			case HEUN :
				for(Agent agent: agents.getAllLocatedAgents())
					for (Point point: ((Body) agent.get("body")).getPoints())
						point.setC(2);
				break;
		default:
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
			switch (_method)
			{
				case SHOVE :
					for(Agent agent: agents.getAllLocatedAgents())
						for (Point point: ((Body) agent.get("body")).getPoints())
							point.shove(dtMech, (double) agent.get("radius"));
					if (vSquare == 0.0) // continue until all overlap is resolved
						tMech = _timeStepSize;
				break;
			
				case EULER :
					/// Euler's method
					for(Agent agent: agents.getAllLocatedAgents())
						for (Point point: ((Body) agent.get("body")).getPoints())
							point.euStep(dtMech, (double) agent.get("radius"));
					tMech += dtMech;
					break;
				
		// NOTE : higher order ODE solvers don't like time Leaping.. be careful.
				case HEUN :
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
					if (agent.isAspect("stochasticStep"))
						agent.event("stochasticMove", dtMech);
				}
			nstep++;
		}
		if(this._debugMode)
			System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
