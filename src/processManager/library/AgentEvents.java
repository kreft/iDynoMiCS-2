/**
 * 
 */
package processManager.library;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;

/**
 * \brief Simple process that asks all agents to perform events by name.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentEvents extends ProcessManager
{
	/**
	 * XML tag for the list of names this is responsible for.
	 */
	// TODO move this to XmlRef or AspectRef?
	public static String EVENT_NAMES = "eventNames";
	
	/**
	 * The names of all agent events this is responsible for.
	 */
	protected String[] _eventNames = new String[0];

	@Override
	public void init(EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		this.init(environment, agents, compartmentName);
		this._eventNames = this.getStringA(EVENT_NAMES);
	}
	
	@Override
	protected void internalStep()
	{
		for ( Agent agent : this._agents.getAllAgents() )
			for ( String eventName : this._eventNames )
				agent.event(eventName);
	}
}
