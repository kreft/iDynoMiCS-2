package processManager.library;

import java.util.LinkedList;

import agent.Agent;

public class AgentsArriveInChemostat extends ArrivalProcess {

	@Override
	public void agentsArrive(LinkedList<Agent> arrivals)
	{
		for (Agent a : this._arrivals)
		{
			this._agents.addAgent(a);
		}
	}
}