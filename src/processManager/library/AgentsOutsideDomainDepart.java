package processManager.library;

import java.util.LinkedList;

import agent.Agent;
import processManager.ProcessDeparture;


/**
 * Any agents that are outside the computational domain are removed. Furthermore
 * any agents that are colliding with a non-solid boundary are removed.
 *
 * //FIXME [Bas] periodic boundaries are also non-solid! those agents should not be removed!
 *
 * This process manager simply returns an empty list, allowing the super-class
 * ProcessDeparture to remove these agents as it does by default. However, using
 * this class allows users to direct these agents to a particular destination,
 * and prevents the generation of warning messages when the super class finds
 * agents outside or leaving the computational domain.
 * @author Tim
 *
 */
public class AgentsOutsideDomainDepart extends ProcessDeparture {

	@Override
	protected LinkedList<Agent> agentsDepart() {
		
		LinkedList<Agent> agentsToDepart = new LinkedList<Agent>();
		
		return agentsToDepart;
	}
	
}
