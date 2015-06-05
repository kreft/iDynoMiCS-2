package agent.activity;

import agent.Agent;

public abstract class Activity {
	
	/**
	 * 
	 */
	protected String _name;
	
	public abstract boolean prerequisites(Agent agent);
	
	public abstract void execute(Agent agent);
	
	public abstract void execute(Agent agent, Agent secondActor);
}
