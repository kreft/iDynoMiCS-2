package processManager.library;

import java.util.LinkedList;

import agent.Agent;
import processManager.ProcessArrival;

/**
 * Simple arrival process for dimensionless compartments.
 * 
 * @author Tim Foster - trf896@student.bham.ac.uk
 *
 */

public class AgentsArriveInChemostat extends ProcessArrival {

	@Override
	public void agentsArrive(LinkedList<Agent> arrivals)
	{
		for (Agent a : this._arrivals)
		{
			this._agents.addAgent(a);
		}
	}
}