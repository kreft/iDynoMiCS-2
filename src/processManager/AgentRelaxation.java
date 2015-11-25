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

public class AgentRelaxation implements ProcessManager {
	protected String _name;
	
	protected int _priority;
	
	protected double _timeForNextStep = 0.0;
	
	protected double _timeStepSize;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	public void init()
	{
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public String getName()
	{
		return this._name;
	}
	
	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	public int getPriority()
	{
		return this._priority;
	}
	
	public void setTimeForNextStep(double newTime)
	{
		this._timeForNextStep = newTime;
	}
	
	public double getTimeForNextStep()
	{
		return this._timeForNextStep;
	}
	
	public void setTimeStepSize(double newStepSize)
	{
		this._timeStepSize = newStepSize;
	}
	
	public double getTimeStepSize()
	{
		return this._timeStepSize;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param boundaries
	 */
	public void showBoundaries(Collection<Boundary> boundaries)
	{
		
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step(EnvironmentContainer environment, AgentContainer agents)
	{
		//System.out.println("STEP");//bughunt
		//System.out.println("timeForNextStep = "+_timeForNextStep);//bughunt
		//System.out.println("timeStepSize = "+_timeStepSize);//bughunt
		/*
		 * This is where subclasses of Mechanism do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(environment, agents);
		/*
		 * Increase the 
		 */
		this._timeForNextStep += this._timeStepSize;
		//System.out.println("timeForNextStep = "+_timeForNextStep);//bughunt
	}
	
	public void internalStep(EnvironmentContainer environment,
											AgentContainer agents) {
		// FIXME work in progress
		// Reset Mechanical stepper
		double dtMech 	= 0.00001; // initial time step
		double tMech	= 0.0;
		int nstep		= 0;
		double tStep	= _timeStepSize;
		double maxMovement		= 0.1;
		// Mechanical relaxation
		while(tMech < tStep) 
		{			
			agents.refreshSpatialRegistry();
			double vSquare = 0.0;
			
			// Calculate forces
			for(Agent agent: agents.getAllLocatedAgents()) 
			{
				//agent.innerSprings();
				for(Agent neighbour: agents._agentTree.search(
						(float[]) agent.get("lowerBoundingBox"), /// Add extra margin for pulls!!!
						(float[]) agent.get("dimensionsBoundingBox"))) 
				{
					if (agent.UID() > neighbour.UID())
						{
						Volume.neighbourInteraction(
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
				+ " shove iterations");
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer report()
	{
		StringBuffer out = new StringBuffer();
		
		return out;
	}
	
}
