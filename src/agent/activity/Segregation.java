package agent.activity;

/**
 * TODO: we need to put some thought in how we want to implement segregation of
 * internal vectors (plasmids, virus)
 */

import agent.Agent;
import utility.ExtraMath;

public class Segregation extends Activity {
	
	public boolean prerequisites(Agent agent)
	{
		return true;
	}
	
	public void execute(Agent agent) {
		// this is an interaction activity, thus we need two actors
	}

	@Override
	public void execute(Agent agent, Agent child) {
		if (prerequisites(agent)) 
		{

		}
	}
}
