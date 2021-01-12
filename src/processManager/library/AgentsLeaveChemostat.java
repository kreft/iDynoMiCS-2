package processManager.library;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import agent.Agent;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import referenceLibrary.AspectRef;
import utility.ExtraMath;
import utility.Helper;


public class AgentsLeaveChemostat extends DepartureProcess
{
	
	private String DETACHMENT_RATE = AspectRef.detachmentRate;
	
	private double _detachmentRate;
	

	@Override
	public void init( Element xmlElem, EnvironmentContainer environment, 
				AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		/* FIXME replace with obtain value as they are case dependent */
		this._detachmentRate = Helper.setIfNone( 
				this.getDouble( DETACHMENT_RATE ), 0.1 );
	}
	
	/**
	 * 
	 * With agent removal over time t dictated by rate r the number of remaining
	 * Agents A can be expressed as:
	 * 
	 * dA/dt = -rA -> A(t) = A(0)*exp(-rt)
	 * 
	 * We only consider removal from the biofilm surface and refer to this as 
	 * detachment.
	 */
	@Override
	public LinkedList<Agent> agentsDepart()
	{
		LinkedList<Agent> departures = new LinkedList<Agent>();
		List<Agent> agentList = _agents.getAllLocatedAgents();
		double e = Math.exp( ( - this.getTimeStepSize() *
				this._detachmentRate)); 
		for ( Agent a : agentList )
		{
			if( ExtraMath.getUniRandDbl() > e )
			{
				departures.add(a);
			}
		}
		return departures;
	}
}