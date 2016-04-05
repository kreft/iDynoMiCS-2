package processManager;

import java.util.List;
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
	double dtBase;	
	double maxMovement;	
	method _method;
	boolean timeLeap;
	
	Collision iterator;
	
//	ConcurrentWorker worker = new ConcurrentWorker();
	
	@Override
	public void init(Element xmlElem)
	{
		super.init(xmlElem);
		/**
		 * Obtaining relaxation parameters
		 */
		dtBase		= Helper.setIfNone(getDouble("dtBase"),0.002);	
		maxMovement	= Helper.setIfNone(getDouble("maxMovement"),0.01);	
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
		 * Updated bodies thus update spatial tree
		 */
		agents.refreshSpatialRegistry();
		
//		worker.executeTask(new AgentInteraction(agents));
		/* FIXME This could also be created just once if the AgentContainer would be 
		 * available with the initiation of the processManager
		 */
		iterator = new Collision(null, agents.getShape());
		
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
			
			double searchDist = (agent.isAspect("searchDist") ?
					agent.getDouble("searchDist") : 0.0);
			
			/**
			 * perform neighborhood search and perform collision detection and
			 * response 
			 */
			for(Agent neighbour: agents.treeSearch(

					((Body) agent.get(NameRef.agentBody)).getBoxes(
							searchDist)))
			{
				if (agent.identity() > neighbour.identity())
				{
					
					agent.event("evaluatePull", neighbour);
					Double pull = agent.getDouble("#curPullDist");
					
					if (pull == null || pull.isNaN())
						pull = 0.0;
					
					for (Surface s : ((Body) agent.get("body")).getSurfaces())
					{
						for (Surface t : ((Body) neighbour.get("body")).getSurfaces())
						{
							iterator.collision(s, t, pull);
						}
					}
				}
			}
			
			/*
			 * Boundary collisions
			 */
			for(Surface s : agents.getShape().getSurfaces())
			{
				for (Surface t : ((Body) agent.get("body")).getSurfaces())
				{
					iterator.collision(s, t, 0.0);
				}
			}
		}
	}
	

	protected void internalStep(EnvironmentContainer environment,
											AgentContainer agents) {
		
		/**
		 * Update agent body now required
		 */
		for(Agent agent: agents.getAllLocatedAgents()) 
		{
			agent.event(NameRef.bodyUpdate);
			agent.event("divide");
			agent.event("epsExcretion");
		}
		
		/* FIXME This could also be created once if the AgentContainer would be 
		 * available with the initiation of the processManager
		 */
//		worker.executeTask(new UpdateAgentBody(agents));

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
			{
				for (Point point: ((Body) agent.get("body")).getPoints())
					if ( Vector.normSquare(point.dxdt((double) agent.get("radius"))) > vSquare )
						vSquare = Vector.normSquare(point.dxdt((double) agent.get("radius")));			
			}
			
			// FIXME this assumes linear force scaling improve..
			vSquare = vSquare * Math.pow(iterator.getMaxForceScalar(), 2.0);
			
			for(Agent agent: agents.getAllLocatedAgents())
			{
				if (agent.isAspect("stochasticDirection"))
				{
					double[] move = (double[]) agent.get("stochasticDirection");
					vSquare = Math.max(Vector.dotProduct(move,move), vSquare);
				}
			}
			// time Leaping set the time step to match a max traveling distance
			// divined by 'maxMovement', for a 'fast' run.
			if(timeLeap) 
				dtMech = maxMovement / (Math.sqrt(vSquare)+0.001);   
			
			// prevent to relaxing longer than the global _timeStepSize
			if(dtMech > _timeStepSize-tMech)
				dtMech = _timeStepSize-tMech;
			
			for(Agent agent: agents.getAllLocatedAgents())
			{
				if (agent.isAspect("stochasticStep"))
					agent.event("stochasticMove", dtMech);
			}
			
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
							point.euStep(dtMech, agent.getDouble("radius"));
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
				}
			nstep++;
		}
		if(this._debugMode)
			System.out.println(agents.getNumAllAgents() + " after " + nstep
				+ " iterations");
	}	
}
