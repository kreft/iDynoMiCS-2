package agent.activity;

/**
 * TODO: we need to put some thought in how we want to implement segregation of
 * internal vectors (plasmids, virus)
 */

import agent.Agent;

public class Segregation extends Activity {
	
	public boolean prerequisites(Agent[] agents)
	{
		return true;
	}
	
	public void execute(Agent[] agents, Double timestep) {
	
	}
}
