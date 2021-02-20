package processManager.library;

import java.util.LinkedList;

import agent.Agent;
import processManager.ProcessDeparture;

public class AgentsOutsideDomainDepart extends ProcessDeparture {

	@Override
	protected LinkedList<Agent> agentsDepart() {
		LinkedList<Agent> agentsToDepart = super.agentsLeavingDomain();
		
		return agentsToDepart;
	}
	
}
