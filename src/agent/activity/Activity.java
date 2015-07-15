package agent.activity;

import agent.Agent;

/*
 * An activity is any process that is governed by an agent. Any activity 
 * implements at least two methods. (1) prerequisites: a method that only 
 * returns true if all conditions that are required for the activity are met and
 * (2) execute: a method that performs the task of the activity.
 */
public abstract class Activity {
	
	/**
	 * 
	 */
	protected String _name;
	
	/*
	 * Returns true if all prerequisites for the activity are met.
	 */
	public abstract boolean prerequisites(Agent[] agents);
	
	/*
	 * Performs the task of the activity if the prerequisites are met.
	 */
	public abstract void execute(Agent[] agents, Double timestep);
}
